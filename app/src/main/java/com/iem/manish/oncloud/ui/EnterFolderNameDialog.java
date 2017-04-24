package com.iem.manish.oncloud.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.iem.manish.oncloud.R;

public class EnterFolderNameDialog extends Activity {

    Button createFolderButton;
    Button cancelButton;
    EditText fileNameText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_folder_name_dialog);
        createFolderButton = (Button)findViewById(R.id.confirmFolder);
        cancelButton = (Button)findViewById(R.id.cancelFolder);
        fileNameText = (EditText)findViewById(R.id.editText);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterFolderNameDialog.this.setResult(Activity.RESULT_CANCELED);
                EnterFolderNameDialog.this.finish();
            }
        });
        createFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("newName", fileNameText.getText() + "");
                EnterFolderNameDialog.this.setResult(Activity.RESULT_OK, intent);
                EnterFolderNameDialog.this.finish();
            }
        });
    }
}
