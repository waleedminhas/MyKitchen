package comp5216.sydney.edu.au.ourkitchen.utils;


import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Empty data observer class, will show an empty view to notify the user the recycler view is empty.
 */
public final class EmptyDataObserver extends RecyclerView.AdapterDataObserver {
    private final View emptyView;
    private final RecyclerView recyclerView;

    /**
     * Constructor
     *
     * @param rv recycler view
     * @param ev empty view
     */
    public EmptyDataObserver(@Nullable RecyclerView rv, @Nullable View ev) {
        this.recyclerView = rv;
        this.emptyView = ev;
        this.checkIfEmpty();
    }

    /**
     * Checks if the recycler view is empty.
     */
    private void checkIfEmpty() {
        if (this.emptyView != null) {
            if (this.recyclerView.getAdapter() != null) {
                boolean emptyViewVisible = this.recyclerView.getAdapter().getItemCount() == 0;
                this.emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
                this.recyclerView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChanged() {
        super.onChanged();
        this.checkIfEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        super.onItemRangeChanged(positionStart, itemCount);
    }
}
