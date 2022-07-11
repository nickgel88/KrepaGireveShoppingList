package com.example.krepagireveshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.krepagireveshoppinglist.Objects.Category;
import com.example.krepagireveshoppinglist.Objects.Product;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{
    private long mLastClickTime;
    Integer permissionParent;
    ArrayList<Product> products;
    ArrayList<Category> categories;
    Realm realm;
    String FilePath, selectedID,selectedCategoryID;
    AlertDialog mAlertDialog;
    Button positiveButton;
    RecyclerViewItemsAdapter itemsAdapter;
    RecyclerView listView;
    MenuItem saveFile, openFile, shareFile, createProduct, removeProduct;
    DrawerLayout drawerLayout;
    private static final int CHOOSE_FILE_REQUESTCODE = 8777;
    private static final int PICKFILE_RESULT_CODE = 8778;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        try {
            Realm.init(getApplicationContext());
            realm = Realm.getDefaultInstance();
            mLastClickTime = 0;
            listView = findViewById(R.id.list_products);

            products = new ArrayList<>();
            categories = new ArrayList<>();
            /*for (Product product : realm.where(Product.class).findAll()) {
                products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
            }*/
            for (Category category : realm.where(Category.class).findAll()) {
                categories.add(new Category(category.getID(), category.getName()));
            }
            if(categories.size()==0){
                categories.add(new Category("0", "Χωρίς Κατηγορία"));
            }
            openList();
            RealmResults<Category> categories=realm.where(Category.class).findAll();
            for (Category cat:categories) {
                ((NavigationView) findViewById(R.id.navigation)).getMenu().getItem(0).getSubMenu().add(cat.getName());
            }
            this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            ((NavigationView) findViewById(R.id.navigation)).setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    int itemId = menuItem.getItemId();
                    String title =String.valueOf(menuItem.getTitle());
                    if (title.equals("Χωρίς Κατηγορία")) {
                        try {
                            selectedCategoryID="0";
                            products = new ArrayList<>();
                            for (Product product : realm.where(Product.class).findAll()) {
                                if(product.getMyCategory()==null||product.getMyCategory().getID().equals("0")){
                                    products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                                }
                            }
                            openList();
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }else{
                        products = new ArrayList<>();
                        selectedCategoryID=realm.where(Category.class).equalTo("Name",(String)title).findFirst().getID();
                        for (Product product : realm.where(Product.class).equalTo("myCategory.Name",(String)title).findAll()) {
                            products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                        }
                        openList();
                    }
                    if (drawerLayout.isDrawerOpen((int) GravityCompat.START)) {
                        drawerLayout.closeDrawer((int) GravityCompat.START);
                    }
                    drawerLayout.closeDrawers();
                    return false;
                }
            });

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.bar_items, menu);
            openFile = menu.findItem(R.id.openFile);
            saveFile = menu.findItem(R.id.saveFile);
            shareFile = menu.findItem(R.id.shareFile);
            createProduct = menu.findItem(R.id.createProduct);
            removeProduct = menu.findItem(R.id.removeProduct);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.openFile:
                    try {
                        if (Build.VERSION.SDK_INT < 23) {
                            // your code
                            readFile();
                        } else {
                            if (MainActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                permissionParent = 1;
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                            } else {
                                readFile();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    return super.onOptionsItemSelected(item);

                case R.id.saveFile:
                    try {

                        if (Build.VERSION.SDK_INT < 23) {
                            // your code
                            writeToFile();
                            {
                                Toast.makeText(MainActivity.this, "Aποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (MainActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                permissionParent = 1;
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                            } else {
                                writeToFile();
                                {
                                    Toast.makeText(MainActivity.this, "Αποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    return super.onOptionsItemSelected(item);

                case R.id.shareFile:
                    writeToFile();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    //intent.putExtra(Intent.EXTRA_TEXT,"Demo Title");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        Uri path = FileProvider.getUriForFile(MainActivity.this, "com.example.krepagireveshoppinglist", new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KrepaGireveShoppingList/ShoppingList.json"));
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "KrepaGireveShoppingList/ShoppingList.json")));
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    intent.setType("text/*");
                    startActivity(intent);
                    return super.onOptionsItemSelected(item);
                case R.id.createProduct:
                    createNewItem();
                    return super.onOptionsItemSelected(item);
                case R.id.removeProduct:
                    removeItem();
                    return super.onOptionsItemSelected(item);
                case R.id.createCategory:
                    createNewCategory();
                    return super.onOptionsItemSelected(item);
                case R.id.removeCategory:
                    removeCategory();
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
        return false;
    }

    public void readFile() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Fix no activity available
        try{
            super.onActivityResult(requestCode, resultCode, data);
            if (data == null)
                return;
            switch (requestCode) {
                case PICKFILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        String responce;
                        FilePath = data.getData().getPath();
                        try {
                            String extension = FilePath.substring(FilePath.lastIndexOf("."));
                            if (extension.equals(".json")) {
                                Uri uri = data.getData();
                                StringBuilder stringBuilder = new StringBuilder();
                                try (InputStream inputStream =
                                             getContentResolver().openInputStream(uri);
                                     BufferedReader reader = new BufferedReader(
                                             new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        stringBuilder.append(line);
                                    }
                                }
                                responce = stringBuilder.toString();
                                JSONObject jsonObject = new JSONObject(responce);
                                final JSONArray jsonArray = jsonObject.getJSONArray("ShoppingList");
                                if (jsonArray.length() > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.createOrUpdateAllFromJson(Product.class, jsonArray);
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                }
                                products = new ArrayList<>();
                                if(!TextUtils.isEmpty(selectedCategoryID)){
                                    if(selectedCategoryID.equals("0")){
                                        for (Product product : realm.where(Product.class).findAll()) {
                                            if(product.getMyCategory()==null||product.getMyCategory().getID().equals("0")){
                                                products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                                            }
                                        }
                                    }else{
                                        for (Product product : realm.where(Product.class).equalTo("myCategory.ID",selectedCategoryID).findAll()) {
                                            products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                                        }
                                    }
                                }
                                createMenu();
                                openList();
                            } else {
                                Toast.makeText(MainActivity.this, "Λάθος τύπος αρχείου", Toast.LENGTH_SHORT).show();
                            }

                            //FilePath is your file as a string
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }catch  (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

    }

    private boolean writeToFile() {
        try {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KrepaGireveShoppingList/");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                // Do something on success
            } else {
                Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                return false;
            }
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KrepaGireveShoppingList/", "ShoppingList.json");
            boolean deleted = true;
            if (file.exists()) {
                File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KrepaGireveShoppingList/ShoppingList.json");
                deleted = file2.delete();
            }
            ArrayList<Product> products1=new ArrayList<Product>();
            for (Product product : realm.where(Product.class).findAll()) {
                products1.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
            }
            FileOutputStream stream = new FileOutputStream(file);
            try {

                Gson gson = new Gson();
                JSONArray array = new JSONArray();
                String json="";
                for (Product product : realm.where(Product.class).findAll()) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("ID", product.getID());
                        object.put("Name" , product.getName());
                        object.put("Balance" , product.getBalance());
                        object.put("ToBuy" , product.getToBuy());
                        object.put("isUrgent" , product.isUrgent());
                        JSONObject il = new JSONObject();
                        if(product.getMyCategory()!=null){
                            il.put("ID" , product.getMyCategory().getID());
                            il.put("Name" , product.getMyCategory().getName());
                        }else{
                            il.put("ID" , "0");
                            il.put("Name" , "Χωρίς Κατηγορία");
                        }
                        object.put("myCategory" , il);
                        array.put(object);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                String finaljson = "{ShoppingList:" + array + "}";

                stream.write(finaljson.getBytes());


            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return false;
            } finally {
                stream.close();
            }
            return true;
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString(), e);
            return false;
        }
    }

    public void openList() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);
        itemsAdapter = new RecyclerViewItemsAdapter(this, this, products);
        listView.setAdapter(itemsAdapter);
        itemsAdapter.notifyDataSetChanged();
    }

    public void createMenu() {
        try {
            ((NavigationView) findViewById(R.id.navigation)).getMenu().getItem(0).getSubMenu().clear();
            RealmResults<Category> categories=realm.where(Category.class).findAll();
            for (Category cat:categories) {
                ((NavigationView) findViewById(R.id.navigation)).getMenu().getItem(0).getSubMenu().add(cat.getName());
            }
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void createNewItem() {

        try {
//            String message = gVar.getMyFInvoice().getMyAddress().getEmail()== null || gVar.getMyFInvoice().getMyAddress().getEmail().equals("")?"Σε ποιο e-mail θέλετε να σταλεί η είσπραξη;":"Η είσπραξη θα σταλεί στο : "+gVar.getMyFInvoice().getMyAddress().getEmail()+"\nΑν θέλετε να σταλεί και κάπου αλλού προσθέστε το λογαριασμό e-mail.";
            String message = "Δημιουργία νέου είδους!";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);
            final int sdk = android.os.Build.VERSION.SDK_INT;

            final EditText input1 = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            input1.setLayoutParams(lp);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input1.setHint("Όνομα προϊόντος");


            layout.addView(input1);

            final AppCompatSpinner spinner = new AppCompatSpinner(this);
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Category category = (Category) parent.getSelectedItem();
                    selectedCategoryID = category.getID();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            spinner.setLayoutParams(lp);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


            layout.addView(spinner);

            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Δημιουργία", null);
            alertDialog.setNegativeButton("Άκυρο", null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            //alertDialog.setMessage(message);
            alertDialog.setTitle(message);


            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);
                    positiveButton.setTextColor(R.color.colorPrimary);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                    negativeButton.setTextColor(R.color.colorPrimary);
                }
            });

            mAlertDialog.show();
            positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(input1.getText().toString())) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {
                                mLastClickTime = SystemClock.elapsedRealtime();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        try {
                                            realm.copyToRealm(new Product(UUID.randomUUID().toString(), input1.getText().toString(), "0", "0",false,realm.where(Category.class).equalTo("ID",selectedCategoryID).findFirst()));
                                            products=new ArrayList<>();
                                            if(!TextUtils.isEmpty(selectedCategoryID)){
                                                if(selectedCategoryID.equals("0")){
                                                    for (Product product : realm.where(Product.class).findAll()) {
                                                        if(product.getMyCategory()==null||product.getMyCategory().getID().equals("0")){
                                                            products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                                                        }
                                                    }
                                                }else{
                                                    for (Product product : realm.where(Product.class).equalTo("myCategory.ID",selectedCategoryID).findAll()) {
                                                        products.add(new Product(product.getID(), product.getName(), product.getBalance(), product.getToBuy(),product.isUrgent(),product.getMyCategory()));
                                                    }
                                                }
                                            }
                                            openList();
                                            mAlertDialog.dismiss();
                                        } catch (Exception e) {
                                            Log.e("asdfg", e.getMessage(), e);
                                        }
                                    }
                                });
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Συμπληρώστε Όνομα", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void removeItem() {

        try {
            String message = "Διαγραφή είδους!";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);

            final AppCompatSpinner spinner = new AppCompatSpinner(this);
            ArrayAdapter<Product> adapter = new ArrayAdapter<Product>(this,
                    android.R.layout.simple_spinner_item, products);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Product product = (Product) parent.getSelectedItem();
                    selectedID = product.getID();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            spinner.setLayoutParams(lp);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


            layout.addView(spinner);
            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Διαγραφη", null);
            alertDialog.setNegativeButton("Άκυρο", null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            //alertDialog.setMessage(message);

            alertDialog.setTitle(message);


            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);
                    positiveButton.setTextColor(R.color.colorPrimary);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                    negativeButton.setTextColor(R.color.colorPrimary);
                }
            });

            mAlertDialog.show();
            positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(selectedID)) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {
                                mLastClickTime = SystemClock.elapsedRealtime();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        try {
                                            realm.where(Product.class).equalTo("ID", selectedID).findFirst().deleteFromRealm();
                                            for (Product product : products) {
                                                if (product.getID().equals(selectedID)) {
                                                    products.remove(product);
                                                    break;
                                                }
                                            }
                                            itemsAdapter.notifyDataSetChanged();
                                            mAlertDialog.dismiss();
                                        } catch (Exception e) {
                                            Log.e("asdfg", e.getMessage(), e);
                                        }
                                    }
                                });

                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Επιλέξτε είδος!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void createNewCategory() {

        try {
//            String message = gVar.getMyFInvoice().getMyAddress().getEmail()== null || gVar.getMyFInvoice().getMyAddress().getEmail().equals("")?"Σε ποιο e-mail θέλετε να σταλεί η είσπραξη;":"Η είσπραξη θα σταλεί στο : "+gVar.getMyFInvoice().getMyAddress().getEmail()+"\nΑν θέλετε να σταλεί και κάπου αλλού προσθέστε το λογαριασμό e-mail.";
            String message = "Δημιουργία νέας κατηγορίας!";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);
            final int sdk = android.os.Build.VERSION.SDK_INT;

            final EditText input1 = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            input1.setLayoutParams(lp);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input1.setHint("Όνομα κατηγορίας");


            layout.addView(input1);
            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Δημιουργία", null);
            alertDialog.setNegativeButton("Άκυρο", null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            //alertDialog.setMessage(message);
            alertDialog.setTitle(message);


            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);
                    positiveButton.setTextColor(R.color.colorPrimary);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                    negativeButton.setTextColor(R.color.colorPrimary);
                }
            });

            mAlertDialog.show();
            positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(input1.getText().toString())) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {
                                mLastClickTime = SystemClock.elapsedRealtime();
                                if (categories.size() > 0) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.copyToRealm(new Category(UUID.randomUUID().toString(), input1.getText().toString()));
                                                categories.add(new Category(UUID.randomUUID().toString(), input1.getText().toString()));
                                                itemsAdapter.notifyDataSetChanged();
                                                mAlertDialog.dismiss();
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                    createMenu();
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.copyToRealm(new Category(UUID.randomUUID().toString(), input1.getText().toString()));
                                                categories.add(new Category(UUID.randomUUID().toString(), input1.getText().toString()));
                                                itemsAdapter.notifyDataSetChanged();
                                                mAlertDialog.dismiss();
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });
                                    createMenu();
                                }
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Συμπληρώστε Όνομα", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void removeCategory() {

        try {
            String message = "Διαγραφή κατηγορίας!";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);

            final AppCompatSpinner spinner = new AppCompatSpinner(this);
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Category category = (Category) parent.getSelectedItem();
                    selectedCategoryID = category.getID();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            spinner.setLayoutParams(lp);
//            input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


            layout.addView(spinner);
            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Διαγραφη", null);
            alertDialog.setNegativeButton("Άκυρο", null);
//        alertDialog.setNeutralButton("Άκυρο",null);
            //alertDialog.setMessage(message);

            alertDialog.setTitle(message);


            mAlertDialog = alertDialog.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTransformationMethod(null);
                    positiveButton.setTextColor(R.color.colorPrimary);

                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setTransformationMethod(null);
                    negativeButton.setTextColor(R.color.colorPrimary);
                }
            });

            mAlertDialog.show();
            positiveButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (!TextUtils.isEmpty(selectedCategoryID)) {
                            if(realm.where(Product.class).equalTo("myCategory.ID",selectedCategoryID).findAll().size()>0){
                                Toast.makeText(MainActivity.this, "Υπάρχουν είδη σε αυτή την κατηγορία,δεν γίνεται να διαγραφεί!", Toast.LENGTH_SHORT).show();
                            }else{
                                if (SystemClock.elapsedRealtime() - mLastClickTime > 5000) {
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.where(Category.class).equalTo("ID", selectedCategoryID).findFirst().deleteFromRealm();
                                                for (Category category : categories) {
                                                    if (category.getID().equals(selectedCategoryID)) {
                                                        categories.remove(category);
                                                        break;
                                                    }
                                                }
                                                createMenu();
                                                mAlertDialog.dismiss();
                                            } catch (Exception e) {
                                                Log.e("asdfg", e.getMessage(), e);
                                            }
                                        }
                                    });

                                }

                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Επιλέξτε κατηγορία!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}
