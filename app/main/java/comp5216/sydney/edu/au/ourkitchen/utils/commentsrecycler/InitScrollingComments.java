package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;

import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Comment;

/**
 * Helper class to initialise the comments for a post and make the recycler view responsive to
 * scrolling
 */
public class InitScrollingComments {
    /**
     * Helper method to get comment from the newsfeed, will observe changes in the firestore
     * database
     *
     * @param query                Firestore query
     * @param isNewQuery           true if a new query, false otherwise
     * @param commentListViewModel comment list view model
     * @param activity             current activity
     * @param commentsAdapter      comment adapter
     * @param commentList          list of comments
     */
    public static void getComments(Query query, boolean isNewQuery,
                                   CommentListViewModel commentListViewModel,
                                   FragmentActivity activity, CommentsAdapter commentsAdapter,
                                   List<Comment> commentList) {
        CommentListLiveData commentListLiveData =
                commentListViewModel.getCommentListLiveData(query, isNewQuery);
        if (commentListLiveData != null && activity != null) {
            commentListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Comment addedComment = operation.comment;
                        addComment(addedComment, commentList);
                        break;

                    case R.string.modified:
                        Comment modifiedComment = operation.comment;
                        modifyComment(modifiedComment, commentList);
                        break;

                    case R.string.removed:
                        Comment removedComment = operation.comment;
                        removeComment(removedComment, commentList);
                }
                commentsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Updates the comment list when a comment is added so the recycler view will update
     *
     * @param addedComment comment to add to recycler view
     * @param commentList  list of comments
     */
    private static void addComment(Comment addedComment, List<Comment> commentList) {
        commentList.add(addedComment);
    }

    /**
     * Updates the comment list when a comment is modified so the recycler view will update
     *
     * @param modifiedComment a modified comment
     * @param commentList     list of comments
     */
    private static void modifyComment(Comment modifiedComment, List<Comment> commentList) {
        for (int i = 0; i < commentList.size(); i++) {
            Comment currentComment = commentList.get(i);
            if (modifiedComment != null && currentComment != null && currentComment.getId().equals(modifiedComment.getId())) {
                commentList.remove(currentComment);
                commentList.add(i, modifiedComment);
            }
        }
    }

    /**
     * Removes a comment from the comment list so it is reflected in the recycler view.
     *
     * @param removedComment comment to remove from list
     * @param commentList    list of comments
     */
    private static void removeComment(Comment removedComment, List<Comment> commentList) {
        for (int i = 0; i < commentList.size(); i++) {
            Comment currentComment = commentList.get(i);
            if (currentComment.getId().equals(removedComment.getId())) {
                commentList.remove(currentComment);
            }
        }
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling          true if the user is scrolling, false otherwise
     * @param currentQuery         current Firestore query
     * @param commentListViewModel comment list view model
     * @param activity             current activity
     * @param commentsAdapter      comment adapter
     * @param commentList          list of comments
     * @param commentsRecyclerView comments recycler view
     * @param layoutManager        layout manager
     */
    public static void initRecyclerViewOnScrollListener(boolean[] isScrolling, Query currentQuery
            , CommentListViewModel commentListViewModel, FragmentActivity activity,
                                                        CommentsAdapter commentsAdapter,
                                                        List<Comment> commentList,
                                                        RecyclerView commentsRecyclerView,
                                                        LinearLayoutManager layoutManager) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisibleCommentPosition = layoutManager.findFirstVisibleItemPosition();
                    int visibleCommentCount = layoutManager.getChildCount();
                    int totalCommentCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisibleCommentPosition + visibleCommentCount == totalCommentCount)) {
                        isScrolling[0] = false;
                        getComments(currentQuery, false, commentListViewModel, activity,
                                commentsAdapter, commentList);
                    }
                }
            }
        };
        commentsRecyclerView.addOnScrollListener(onScrollListener);
    }
}
