package ramola.search;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 10;
    private GoogleApiClient mGoogleApiClient;
private Button signout,revokedAccess;
private SignInButton signInButton;
private int mSignInProgress,mSignInError;
private final int STATE_SIGNED_IN=0;
private final int STATE_SIGNED_OUT=1;
private final int STATE_REVOKED_ACCESS=2;
private PendingIntent mSignInIntent;
private ImageLoader imageLoader;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton= (SignInButton) findViewById(R.id.SignInBtn);
        signout= (Button) findViewById(R.id.SignOutBtn);
        revokedAccess= (Button) findViewById(R.id.RevokedAccess);
        imageView= (ImageView) findViewById(R.id.image);
       imageLoader=VolleySingleton.getInstance().getImageLoader();
        UpdateUi(false);

        signInButton.setOnClickListener(this);
        revokedAccess.setOnClickListener(this);
        signInButton.setOnClickListener(this);

        mGoogleApiClient=buildApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null)
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        mGoogleApiClient.disconnect();
    }

    private GoogleApiClient buildApiClient(){
    return new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
            .build();
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        if(!mGoogleApiClient.isConnecting()){
            switch (view.getId()){
                case R.id.SignInBtn:
                    resolveSignInerror();
                    Toast.makeText(this,"Sign in",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.SignOutBtn:
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                    break;
                case R.id.RevokedAccess:
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                    mGoogleApiClient=buildApiClient();
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }

    private void resolveSignInerror() {
        if(mSignInIntent!=null){
           mSignInProgress=STATE_REVOKED_ACCESS;
            try {
                startIntentSenderForResult(mSignInIntent.getIntentSender(),RC_SIGN_IN,null,0,0,0);
            } catch (IntentSender.SendIntentException e) {
                mSignInProgress=STATE_SIGNED_IN;
                mGoogleApiClient.connect();
            }
        }
        else {
            Toast.makeText(this,"ERROR OCCUR",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        UpdateUi(true);
        mSignInProgress=STATE_SIGNED_OUT;
        if(mGoogleApiClient!=null&&mGoogleApiClient.isConnected()) {
           Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String s=person.getImage().getUrl();
            LoadImage(s.substring(0,s.length()-2)+"400");
          Toast.makeText(this, "Your are Logged " + person.getDisplayName(), Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
         if(mSignInProgress!=STATE_REVOKED_ACCESS){
             mSignInIntent=connectionResult.getResolution();
             mSignInError=connectionResult.getErrorCode();
             if(mSignInProgress==STATE_SIGNED_IN){
                 resolveSignInerror();
             }
         }
        onSignedOut();
    }

    private void onSignedOut() {
        UpdateUi(false);
    }

    private void UpdateUi(boolean result){
        if(!result){
            signInButton.setVisibility(View.VISIBLE);
            signout.setVisibility(View.GONE);
            revokedAccess.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }
        else
        {
            signInButton.setVisibility(View.GONE);
            signout.setVisibility(View.VISIBLE);
            revokedAccess.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_OK){
                mSignInProgress=STATE_SIGNED_IN;
            }
            else {
            mSignInProgress=STATE_SIGNED_OUT;}
            if(!mGoogleApiClient.isConnecting()){
                mGoogleApiClient.connect();
            }

        }
    }
    private void LoadImage(String s){
        imageLoader.get(s,new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                imageView.setImageBitmap(imageContainer.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                  volleyError.printStackTrace();
            }
        });
    }
}
