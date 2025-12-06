package com.example.p1.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p1.R;
import com.example.p1.models.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenses;
    private Context context;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseLongClick(Expense expense);
    }

    public ExpenseAdapter(Context context, List<Expense> expenses, OnExpenseClickListener listener) {
        this.context = context;
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        // Afficher le titre
        holder.title.setText(expense.getTitle() != null ? expense.getTitle() : "Sans titre");

        // Afficher la catégorie
        holder.category.setText(expense.getCategory() != null ? expense.getCategory() : "Non catégorisé");

        // Afficher le montant
        holder.amount.setText(String.format("%.2f DT", expense.getAmount()));

        // Afficher la date
        holder.date.setText(expense.getDate() != null ? expense.getDate() : "");

        // Choisir l'icône selon la catégorie
        setIconByCategory(holder.icon, expense.getCategory());
    }

    private void setIconByCategory(ImageView icon, String category) {
        if (category == null) {
            icon.setImageResource(R.drawable.ic_shopping);
            return;
        }

        String cat = category.toLowerCase();

        if (cat.contains("nourriture") || cat.contains("food") || cat.contains("alimentation")) {
            icon.setImageResource(R.drawable.ic_food);
        } else if (cat.contains("transport")) {
            icon.setImageResource(R.drawable.ic_transport);
        } else if (cat.contains("loisirs") || cat.contains("shopping")) {
            icon.setImageResource(R.drawable.ic_shopping);
        } else if (cat.contains("santé") || cat.contains("health")) {
            icon.setImageResource(R.drawable.ic_food);
        } else if (cat.contains("logement") || cat.contains("home")) {
            icon.setImageResource(R.drawable.ic_transport);
        } else if (cat.contains("éducation") || cat.contains("education")) {
            icon.setImageResource(R.drawable.ic_shopping);
        } else {
            icon.setImageResource(R.drawable.ic_shopping);
        }
    }

    @Override
    public int getItemCount() {
        return expenses != null ? expenses.size() : 0;
    }

    public void updateList(List<Expense> newList) {
        this.expenses = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, category, amount, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.expenseIcon);
            title = itemView.findViewById(R.id.expenseTitle);
            category = itemView.findViewById(R.id.expenseCategory);
            amount = itemView.findViewById(R.id.expenseAmount);
            date = itemView.findViewById(R.id.expenseDate);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExpenseClick(expenses.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onExpenseLongClick(expenses.get(position));
                        return true;
                    }
                }
                return false;
            });
        }
    }
}