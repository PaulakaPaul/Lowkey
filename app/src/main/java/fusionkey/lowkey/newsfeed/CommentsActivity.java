package fusionkey.lowkey.newsfeed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.amazonaws.services.s3.model.S3DataSource;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fusionkey.lowkey.LowKeyApplication;
import fusionkey.lowkey.R;
import fusionkey.lowkey.auth.utils.UserAttributesEnum;
import fusionkey.lowkey.listAdapters.CommentAdapters.CommentAdapter;
import fusionkey.lowkey.listAdapters.NewsfeedAdapter;
import fusionkey.lowkey.main.MainCallback;

public class CommentsActivity extends AppCompatActivity {
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";


    LinearLayout contentRoot;
    RecyclerView rvComments;
    Button button;
    EditText inputTxt;
    LinearLayout llAddComment;
    NewsFeedCallBack newsFeedCallBack;
    List<Comment> commentArrayList;
    ArrayList<Comment> commentsSentList = new ArrayList<>();
    private CommentAdapter commentsAdapter;
    private int drawingStartLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        contentRoot = findViewById(R.id.contentRoot);
        rvComments = findViewById(R.id.rvComments);
        llAddComment = findViewById(R.id.llAddComment);
        button = findViewById(R.id.sendComment);
        inputTxt = findViewById(R.id.chat_input_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }
        populateWithData();
        setupComments();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(inputTxt.getText().toString())) {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    Map<String, String> attributes = LowKeyApplication.userManager.getUserDetails().getAttributes().getAttributes();
                    final String uniqueID = attributes.get(UserAttributesEnum.EMAIL.toString());
                    final String username = attributes.get(UserAttributesEnum.USERNAME.toString());
                    new NewsfeedRequest(username).postComment(getIntent().getStringExtra("timestampID"), true, inputTxt.getText().toString());
                    commentArrayList.add(new Comment("true",String.valueOf(timestamp.getTime()),inputTxt.getText().toString(),username));
                    commentsSentList.add(new Comment("true",String.valueOf(timestamp.getTime()),inputTxt.getText().toString(),username));

                    int newMsgPosition = commentArrayList.size() - 1;
                    commentsAdapter.notifyItemInserted(newMsgPosition);
                    rvComments.scrollToPosition(newMsgPosition);
                    inputTxt.setText("");
                }

            }
        });

    }





    private void populateWithData(){
        Bundle b = getIntent().getExtras();
        try {
            MyParcelable object = b.getParcelable("parcel");
            commentArrayList = object.getArrList();
            commentsAdapter = new CommentAdapter(commentArrayList,this);
            rvComments.setAdapter(commentsAdapter);

        }catch(NullPointerException e){
            Log.e("Error","parcelable object failed");
        }
    }



    private void startIntroAnimation() {
        contentRoot.setScaleY(0.1f);
        contentRoot.setPivotY(drawingStartLocation);
        llAddComment.setTranslationY(100);

        contentRoot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animateContent();
                    }
                })
                .start();
    }

    private void animateContent() {

        llAddComment.animate().translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(200)
                .start();
    }
    private void setupComments() {
        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    commentsAdapter.setAnimationsLocked(true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent retrieveData = new Intent();
        MyParcelable object = new MyParcelable();
        object.setArrList(commentsSentList);
        retrieveData.putExtra("NewComments", object);
        retrieveData.putExtra("ItemID",getIntent().getStringExtra("timestampID"));
        if(commentsSentList!=null)
        setResult(Activity.RESULT_OK,retrieveData);
        else
            setResult(Activity.RESULT_CANCELED,retrieveData);
        finish();

        contentRoot.animate()
                .translationY(Resources.getSystem().getDisplayMetrics().heightPixels)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CommentsActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();

    }

}

