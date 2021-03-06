package fusionkey.lowkey.auth.utils;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidentityprovider.model.ListUsersRequest;
import com.amazonaws.services.cognitoidentityprovider.model.ListUsersResult;
import com.amazonaws.services.cognitoidentityprovider.model.UserType;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CognitoPoolUtils {
    static final String REGION = "eu-central-1"; // Used by Java SDK.
    static final Regions REGIONS = Regions.EU_CENTRAL_1; // Used by Android SDK.
    static final String USER_POOL_ID = "eu-central-1_JoEaRznTH";
    static final String CLIENT_ID = "5s1djmjafqttgk2jb82l3ng3eo";
    static final String CLIENT_SECRET = "tr02eavc68bv2fne6u0bd4mli7trdbmnqicjk6t3o4k2authga";

    private CognitoUserPool userPool;
    private CognitoUser user;
    private CognitoUserDetails userDetails;
    private CognitoUserSession userSession;

    CognitoPoolUtils (Context context)  {
        userPool = new CognitoUserPool(context,
                USER_POOL_ID,
                CLIENT_ID,
                CLIENT_SECRET,
                REGIONS
        );
    }

    public CognitoUserPool getUserPool() {
        return userPool;
    }

    public CognitoUser getUser() {
        return user;
    }

    public synchronized void setUser(String userId) {
        this.user = userPool.getUser(userId);
    }

    public synchronized void setUser(CognitoUser user) {
        this.user = user;
    }

    public synchronized void setUserToNull() {
        this.user = null;
    }

    public CognitoUserDetails getUserDetails() {
        return userDetails;
    }

    public synchronized void setUserDetails(CognitoUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public CognitoUserSession getUserSession() {
        return userSession;
    }

    public synchronized void setUserSession(CognitoUserSession cognitoUserSession) {
        this.userSession = cognitoUserSession;
    }

    public void setAllUserDataToNull() {
        user = null;
        userDetails = null;
        userSession = null;
    }

    public List<UserType> getUsers(final UserAttributesEnum attribute, final String value) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<UserType>> result = executor.submit(new Callable<List<UserType>>() {
            @Override
            public List<UserType> call() throws Exception {
                AmazonCognitoIdentityProviderClient identityUserPoolProviderClient =
                        new AmazonCognitoIdentityProviderClient(
                                new BasicAWSCredentials(AwsAccessKeys.ACCESS_KEY_ID, AwsAccessKeys.ACCESS_SECRET_KEY));
                identityUserPoolProviderClient.setRegion(Region.getRegion(REGION));

                ListUsersRequest listUsersRequest = new ListUsersRequest();
                listUsersRequest.withUserPoolId(USER_POOL_ID);
                if(attribute != null && value != null)
                    listUsersRequest.withFilter(String.format("%s=\"%s\"", attribute.toString(), value));

                ListUsersResult result = identityUserPoolProviderClient.listUsers(listUsersRequest);
                return result.getUsers();
            }
        });

          try {
              return result.get();
          } catch (InterruptedException | ExecutionException e) {
              Log.e("getUsers()", e.getMessage());
              return null;
          } finally {
              executor.shutdown();
          }
    }
}
