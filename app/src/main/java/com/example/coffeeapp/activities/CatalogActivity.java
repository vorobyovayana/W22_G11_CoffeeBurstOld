package com.example.coffeeapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;


import com.example.coffeeapp.databases.ProductDatabase;
import com.example.coffeeapp.databinding.ActivityCatalogBinding;
import com.example.coffeeapp.interfaces.ProductDao;
import com.example.coffeeapp.models.Product;
import com.example.coffeeapp.R;
import com.example.coffeeapp.supplements.RecyclerItemClickListener;
import com.example.coffeeapp.adapters.ProductAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CatalogActivity extends AppCompatActivity {

    private RecyclerView coffeeRecyclerView;
    private RecyclerView teaRecyclerView;

    private ArrayList<Product> coffeeModelArrayList;
    private ArrayList<Product> teaModelArrayList;

    ActivityCatalogBinding binding;
    ProductDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        coffeeRecyclerView = findViewById(R.id.idRVCoffee);
        teaRecyclerView = findViewById(R.id.idRVTea);

        db = Room.databaseBuilder(getApplicationContext(),
                ProductDatabase.class,"Products.db").build();

        coffeeModelArrayList = new ArrayList<>();
        teaModelArrayList = new ArrayList<>();

        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        List<Product> AllProducts = ReadProductsCSV();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        coffeeRecyclerView.setLayoutManager(linearLayoutManager);
        //coffeeRecyclerView.setAdapter(coffeeAdapter);

        teaRecyclerView.setLayoutManager(linearLayoutManager2);
        //teaRecyclerView.setAdapter(teaAdapter);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                ProductDao productDao = db.productDao();
                productDao.insertProductsFromList(AllProducts);
                coffeeModelArrayList = (ArrayList<Product>) db.productDao().GetProductsOfCategory("Coffee");
                teaModelArrayList = (ArrayList<Product>) db.productDao().GetProductsOfCategory("Tea");
                Log.d("Size of the list", String.valueOf(teaModelArrayList.size()));

                //instead of setting the data directly from file
                //we are setting it after getting it from Database
                runOnUiThread(() ->{
                    ProductAdapter coffeeAdapter = new ProductAdapter(this, coffeeModelArrayList);
                    ProductAdapter teaAdapter = new ProductAdapter(this, teaModelArrayList);
                    teaRecyclerView.setAdapter(teaAdapter);
                    coffeeRecyclerView.setAdapter(coffeeAdapter);
                });
            } catch (Exception ex){
                Log.d("CoffeeBurst",ex.getMessage());
            }
        });

        coffeeRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, coffeeRecyclerView,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent intent = new Intent(CatalogActivity.this, ProductCardActivity.class);
                        ProductAdapter.Viewholder mod = (ProductAdapter.Viewholder) coffeeRecyclerView.findViewHolderForAdapterPosition(position);
                        String name = (String) Objects.requireNonNull(mod).productNameTV.getText();
                        intent.putExtra("name", name);
                        int imageId = Objects.requireNonNull(mod).productIV.getId();
                        intent.putExtra("imgId", imageId);
                        System.out.println(name);
                        System.out.println(imageId);
                        startActivity(intent);
                    }
                    @Override public void onLongItemClick(View view, int position) {
                        System.out.println("@");
                    }
                })
        );
    }

    private List<Product> ReadProductsCSV(){
        List<Product> productList = new ArrayList<>();

        InputStream inputStream = getResources().openRawResource(R.raw.catalog);
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(inputStream));
        try {
            // This is header, it's not included
            String productLine;
            if ((productLine = reader.readLine()) != null){

            }
            while ((productLine = reader.readLine()) != null){
                String[] eachProductFields = productLine.split(",");
                Product eachProduct = new Product(eachProductFields[1],
                        eachProductFields[0], eachProductFields[2],
                        Double.parseDouble(eachProductFields[3]), eachProductFields[4]);
                productList.add(eachProduct);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading file " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return productList;
    }
}
