package com.example.krepagireveshoppinglist;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krepagireveshoppinglist.Objects.Product;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class RecyclerViewItemsAdapter extends RecyclerView.Adapter<RecyclerViewItemsAdapter.ViewHolder> {

    Realm realm;
    Context mContext;
    private int finalHeight,finalWidth;
    private DecimalFormat decim = new DecimalFormat("0.00");
    private NumberFormat priceFormat = NumberFormat.getInstance();
    private ArrayList<Product> products;
    private Activity activity;
    private Rect r = new Rect();
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static int DELTA = 50;


    public RecyclerViewItemsAdapter(Context context,Activity activity, ArrayList<Product> products) {
        mContext=context;
        this.products = products;
        this.activity = activity;
        Log.d("asdfg", "in-" + products.size());
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public String ID;
        public TextView Name;
        public EditText Balance;
        public EditText ToBuy;
        public CheckBox isUrgent;
        public int ref;
        Realm realm;
        public BalanceTextListener myBalanceTextListener;
        public ToBuyTextListener myToBuyTextListener;
        public checkedListener myCheckedListener;

        public ViewHolder(View v,BalanceTextListener myBalanceTextListener,ToBuyTextListener myToBuyTextListener,checkedListener myCheckedListener) {
            super(v);
            realm = Realm.getDefaultInstance();
            String ID;

            Name=(TextView)v.findViewById(R.id.product_name);
            Balance=(EditText)v.findViewById(R.id.product_balance);
            ToBuy=(EditText)v.findViewById(R.id.product_toBuy);;
            isUrgent=(CheckBox) v.findViewById(R.id.check_isUrgent);;

            this.myBalanceTextListener = myBalanceTextListener;
            this.myToBuyTextListener = myToBuyTextListener;
            this.myCheckedListener = myCheckedListener;

            Balance.addTextChangedListener(myBalanceTextListener);
            ToBuy.addTextChangedListener(myToBuyTextListener);
            isUrgent.setOnCheckedChangeListener(myCheckedListener);

            int ref;


        }

    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.listview_products, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView,new BalanceTextListener(),new ToBuyTextListener(),new checkedListener());
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try{
            final Product product = products.get(position);
            realm = Realm.getDefaultInstance();


            holder.myBalanceTextListener.updatePosition(holder.getAdapterPosition());
            holder.myToBuyTextListener.updatePosition(holder.getAdapterPosition());
            holder.myCheckedListener.updatePosition(holder.getAdapterPosition());

            // Check if an existing view is being reused, otherwise inflate the view
            final RecyclerViewItemsAdapter.ViewHolder viewHolder;

            final EditText Balance = holder.Balance;
            final EditText ToBuy=holder.ToBuy;
            final CheckBox isUrgent=holder.isUrgent;

            holder.Name.setText(product.getName());
            holder.Balance.setText(product.getBalance());
            holder.ToBuy.setText(product.getToBuy());
            holder.isUrgent.setChecked(product.isUrgent());

            holder.itemView.setTag(holder);


            // Return the completed view to render on screen
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            Toast.makeText(mContext,"Εκτός Σύνδεσης",Toast.LENGTH_SHORT).show();
        }

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return products.size();
    }


    private class BalanceTextListener implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                final String balance=s.toString();
                products.get(position).setBalance(balance);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(Product.class).equalTo("ID",products.get(position).getID()).findFirst().setBalance(balance);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
            } catch (Exception e) {
                Log.d("asdfg", String.valueOf(e));
            }
        }
    }

    private class ToBuyTextListener implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                final String toBuy=s.toString();
                products.get(position).setToBuy(toBuy);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(Product.class).equalTo("ID",products.get(position).getID()).findFirst().setToBuy(toBuy);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
            } catch (Exception e) {
                Log.d("asdfg", String.valueOf(e));
            }

        }
    }

    private class checkedListener implements CompoundButton.OnCheckedChangeListener{

        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
            try {
                products.get(position).setUrgent(isChecked);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(Product.class).equalTo("ID",products.get(position).getID()).findFirst().setUrgent(isChecked);
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                });
                if(isChecked){
                    buttonView.setBackgroundResource(R.drawable.border_red_int_checkbox);
                }else{
                    buttonView.setBackgroundResource(R.drawable.border_in_textview);
                }
            } catch (Exception e) {
                Log.d("asdfg", String.valueOf(e));
            }
        }
    }
}
