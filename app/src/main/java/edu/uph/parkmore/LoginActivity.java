package edu.uph.parkmore;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Sign In/Main Activity
 * @author Samuel I. Gunadi
 */
public class LoginActivity extends Activity
{
    private EditText email_edit;
    private EditText password_edit;
    public TextView register_view;
    public Button login_button;
    public Button server_addr_button;
    private EditText server_addr_edit;


    private static final int PERMISSIONS_ID = 1;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        email_edit = (EditText) findViewById(R.id.login_email_edit);
        password_edit = (EditText) findViewById(R.id.login_password_edit);
        register_view = (TextView) findViewById(R.id.login_register_view);
        login_button = (Button) findViewById(R.id.login_button);
        server_addr_button = (Button) findViewById(R.id.login_server_addr_button);
        server_addr_edit = (EditText) findViewById(R.id.login_server_addr_edit);

        login_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (email_edit.getText().toString().isEmpty() || password_edit.getText().toString().isEmpty())
                {
                    Global.show_alert(LoginActivity.this, "Error", "Enter email and password.");
                    return;
                }
                try
                {
                    new LoginAsyncTask().execute(
                            new JSONObject()
                                    .put("action", "login")
                                    .put("email", email_edit.getText().toString())
                                    .put("password", password_edit.getText().toString())
                                    .toString()
                    );
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        });

        register_view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, PERMISSIONS_ID);
        }

        server_addr_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ParkmoreClient.server_url = server_addr_edit.getText().toString();
            }
        });
    }

   private class LoginAsyncTask extends AsyncTask<String, Void, String>
    {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(LoginActivity.this, "", "Signing in...", true, false);
        }

        @Override
        protected String doInBackground(String... params)
        {
            return ParkmoreClient.send_and_receive_json(params[0]);
        }


        @Override
        protected void onPostExecute(String param)
        {
            if (progress.isShowing())
            {
                progress.dismiss();
            }
            if (param == null)
            {
                Global.show_alert(LoginActivity.this, "Error", "Connection error.");
                return;
            }
            JSONObject json;
            try
            {
                json = new JSONObject(param);
                if (json.getBoolean("success"))
                {
                    Intent i = new Intent(LoginActivity.this, MenuActivity.class);
                    // reset fields
                    email_edit.setText("");
                    password_edit.setText("");
                    startActivity(i);
                }
                else
                {
                    JSONArray error_codes = json.getJSONArray("error_codes");
                    String error_string = "";
                    for (int i = 0; i < error_codes.length(); i++)
                    {
                        error_string += error_codes.getString(i) + " ";
                    }
                    Global.show_alert(LoginActivity.this, "Error", error_string);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Global.show_alert(LoginActivity.this, "Error", "JSON parsing error.");
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_ID:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0)
                {
                    for (int el : grantResults)
                    {
                        if (el != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this, "NEED PERMISSIONS! KTHXBAI.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
            }
            default:
            break;
        }
    }
}
