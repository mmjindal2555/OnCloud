package com.iem.manish.oncloud.ui;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.iem.manish.oncloud.R;

public class EnterFileNameDialog extends Activity {
    Button renameButton;
    Button cancelButton;
    EditText fileNameText;
    String fileSelected;
    String fileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_file_name_dialog);
        renameButton = (Button)findViewById(R.id.rename_button);
        cancelButton = (Button)findViewById(R.id.cancel_rename);
        fileNameText = (EditText)findViewById(R.id.rename_edit_text);
        final Intent intent = new Intent();
        final Intent in = getIntent();
        fileSelected = in.getStringExtra("fileSelected");
        fileName =fileSelected;
        if(fileSelected.charAt(fileSelected.length()-1) == '/'){
            fileName = fileSelected.substring(0,fileSelected.length()-1);
        }
        fileName = fileName.substring(fileName.lastIndexOf('/')+1);
        if(fileName.lastIndexOf('.')>0) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        fileNameText.setText(fileName);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            EnterFileNameDialog.this.setResult(Activity.RESULT_CANCELED);
            EnterFileNameDialog.this.finish();
            }
        });
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                intent.putExtra("fileSelected",fileSelected);
                intent.putExtra("newName", fileNameText.getText()+"");
                EnterFileNameDialog.this.setResult(Activity.RESULT_OK,intent);
                EnterFileNameDialog.this.finish();
            }
        });
    }

}
