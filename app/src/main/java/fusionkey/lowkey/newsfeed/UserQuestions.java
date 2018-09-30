package fusionkey.lowkey.newsfeed;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fusionkey.lowkey.LowKeyApplication;
import fusionkey.lowkey.R;
import fusionkey.lowkey.auth.utils.UserAttributesEnum;
import fusionkey.lowkey.listAdapters.ChatTabViewHolder;
import fusionkey.lowkey.listAdapters.NewsFeedAdapter;
import fusionkey.lowkey.newsfeed.asynctasks.GetYourQuestionsAsyncTask;
import fusionkey.lowkey.newsfeed.models.Comment;
import fusionkey.lowkey.newsfeed.models.NewsFeedMessage;
import fusionkey.lowkey.newsfeed.util.NewsFeedRequest;

public class UserQuestions extends AppCompatActivity {
    NewsFeedAdapter adapter;
    ArrayList<NewsFeedMessage> messages;
    String uniqueID;
    private RecyclerView msgRecyclerView;
    NewsFeedRequest newsFeedRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_questions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        msgRecyclerView = (RecyclerView) findViewById(R.id.chat_listview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(linearLayoutManager);

        //Getting user details from Cognito
        Map<String, String> attributes = LowKeyApplication.userManager.getUserDetails().getAttributes().getAttributes();
        final String id = attributes.get(UserAttributesEnum.USERNAME.toString());
        uniqueID = (attributes.get(UserAttributesEnum.EMAIL.toString()));

        messages = new ArrayList<NewsFeedMessage>();
        adapter = new NewsFeedAdapter(messages,this,msgRecyclerView);
        msgRecyclerView.setAdapter(adapter);
        newsFeedRequest = new NewsFeedRequest(uniqueID);

        adapter.setListener(new NewsFeedAdapter.OnItemClickListenerNews() {
            @Override
            public void onItemClick(ChatTabViewHolder item, View v) {
                int position = item.getAdapterPosition();
                NewsFeedMessage m = adapter.getMsg(position);
                final Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                intent.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
                if(m.getCommentArrayList()!=null) {
                    MyParcelable object = new MyParcelable();
                    object.setArrList(m.getCommentArrayList());
                    object.setMyInt(m.getCommentArrayList().size());
                    intent.putExtra("parcel", object);

                    intent.putExtra("timestampID",m.getTimeStamp());
                }

                startActivityForResult(intent,1);
                overridePendingTransition(0, 0);
            }
        });
        adapter.setDeleteListener(new NewsFeedAdapter.OnDeleteItem() {
            @Override
            public void deleteItem(ChatTabViewHolder item, View v) {
                int position = item.getAdapterPosition();
                NewsFeedMessage m = adapter.getMsg(position);
                new NewsFeedRequest(uniqueID).deleteQuestion(String.valueOf(m.getTimeStamp()));
                adapter.removeItem(position);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        refreshNewsfeed();
    }

    public void refreshNewsfeed(){
        GetYourQuestionsAsyncTask getYourQuestionsAsyncTask = new GetYourQuestionsAsyncTask(messages,msgRecyclerView,adapter, newsFeedRequest);
        getYourQuestionsAsyncTask.execute();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.e("GETHERE", "HERE");
            if(resultCode == Activity.RESULT_OK){
                Bundle b = data.getExtras();
                try {
                    MyParcelable object = b.getParcelable("NewComments");
                    String timestampID = b.getString("ItemID");
                    List<Comment> commentArrayList = object.getArrList();
                    for (NewsFeedMessage m : messages) {
                        Log.e("GETHERE", "HERE FOR ");
                        if (m.getTimeStamp().equals(timestampID)) {
                            Log.e("GETHERE", "HERE IF");
                            for (Comment c : commentArrayList)
                                m.addCommentToList(c);
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (NullPointerException e) {
                    Log.e("Error", "parcelable object failed");
                }
            }else { Log.e("IntentResult","No comments to update");
            }
        }

    }

}
