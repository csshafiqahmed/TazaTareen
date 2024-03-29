package com.codestown.tazatareen.ui.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.codestown.tazatareen.Adapters.CategorySelectAdapter;
import com.codestown.tazatareen.Adapters.SelectableCategoryViewHolder;
import com.codestown.tazatareen.Adapters.SelectableLanguageViewHolder;
import com.codestown.tazatareen.Provider.PrefManager;
import com.codestown.tazatareen.R;
import com.codestown.tazatareen.api.ProgressRequestBody;
import com.codestown.tazatareen.api.apiClient;
import com.codestown.tazatareen.api.apiRest;
import com.codestown.tazatareen.model.ApiResponse;
import com.codestown.tazatareen.model.Category;
import com.codestown.tazatareen.model.Language;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import jp.wasabeef.richeditor.RichEditor;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddYoutubeActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks,SelectableCategoryViewHolder.OnItemSelectedListener ,SelectableLanguageViewHolder.OnItemSelectedListener {

    private EditText edit_text_upload_title;
    private LinearLayout linear_layout_categories;
    private RichEditor edit_text_upload_description;
    private RelativeLayout relative_layout_upload;
    private LinearLayoutManager gridLayoutManagerCategorySelect;
    private RecyclerView recycle_view_selected_category;
    private ImageView image_view_guide_activity_select_image;
    private int PICK_IMAGE = 1002;
    private ProgressDialog pd;
    private String   imageUrl;

    private ArrayList<Category> categoriesListObj = new ArrayList<Category>();
    private CategorySelectAdapter categorySelectAdapter;
    private ProgressDialog register_progress;

    private List<Language> languageList = new ArrayList<Language>();
    private ImageView image_view_add_guide_preview;
    private RelativeLayout relativeLayout_rich_box;
    private TextView text_view_show_rish;
    private ImageView image_view_done_rich;
    private WebView web_view_guide_activity_content;
    private String content = "";
    private ImageView image_view_edit;
    private Spinner spinner_language_select;
    private ImageView image_view_post_activity_save;
    private EditText edit_text_upload_youtube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_add_youtube);
        initView();
        initAction();
        getLanguages();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("New Youtube Video");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void SelectImage() {
        if (ContextCompat.checkSelfPermission(AddYoutubeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddYoutubeActivity.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }else{
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/jpeg");
            startActivityForResult(i, PICK_IMAGE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    public void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(AddYoutubeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {


                if (ActivityCompat.shouldShowRequestPermissionRationale(AddYoutubeActivity.this,   Manifest.permission.READ_CONTACTS)) {
                    Intent intent_status  =  new Intent(getApplicationContext(), PermissionActivity.class);
                    startActivity(intent_status);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    finish();
                } else {
                    Intent intent_status  =  new Intent(getApplicationContext(), PermissionActivity.class);
                    startActivity(intent_status);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    finish();
                }
            }

        }
    }
    private void initView() {
        this.register_progress = ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);
        this.image_view_post_activity_save=(ImageView) findViewById(R.id.image_view_post_activity_save);
        this.image_view_edit=(ImageView) findViewById(R.id.image_view_edit);
        this.web_view_guide_activity_content=(WebView) findViewById(R.id.web_view_guide_activity_content);
        this.image_view_done_rich=(ImageView) findViewById(R.id.image_view_done_rich);
        this.text_view_show_rish=(TextView) findViewById(R.id.text_view_show_rish);
        this.relativeLayout_rich_box=(RelativeLayout) findViewById(R.id.relativeLayout_rich_box);
        this.linear_layout_categories=(LinearLayout) findViewById(R.id.linear_layout_categories);
        pd = new ProgressDialog(AddYoutubeActivity.this);
        pd.setMessage("Uploading Post");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        this.edit_text_upload_description =  (RichEditor) findViewById(R.id.editor);
        this.image_view_add_guide_preview =  (ImageView) findViewById(R.id.image_view_add_guide_preview);
        this.image_view_guide_activity_select_image =  (ImageView) findViewById(R.id.image_view_guide_activity_select_image);
        this.edit_text_upload_title=(EditText) findViewById(R.id.edit_text_upload_title);
        this.edit_text_upload_youtube=(EditText) findViewById(R.id.edit_text_upload_youtube);
        this.relative_layout_upload=(RelativeLayout) findViewById(R.id.relative_layout_upload);

        PrefManager prf= new PrefManager(getApplicationContext());


        final RichEditor mEditor = (RichEditor) findViewById(R.id.editor);

        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        mEditor.setPadding(10, 10, 10, 10);
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setPlaceholder("Insert text here...");
        //mEditor.setInputEnabled(false);


        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(AddYoutubeActivity.this)
                        .setTitle("Choose color")
                        .initialColor(Color.BLACK)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                mEditor.setTextColor(Color.parseColor("#"+Integer.toHexString(selectedColor)));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mEditor.setTextColor(Color.parseColor("#"+Integer.toHexString(selectedColor)));
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(AddYoutubeActivity.this)
                        .setTitle("Choose color")
                        .initialColor(Color.BLACK)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                mEditor.setTextBackgroundColor(Color.parseColor("#"+Integer.toHexString(selectedColor)));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mEditor.setTextBackgroundColor(Color.parseColor("#"+Integer.toHexString(selectedColor)));
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();


            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddYoutubeActivity.this);
                builder.setTitle("Insert Image");
                final EditText input = new EditText(AddYoutubeActivity.this);
                input.setHint("Image Link");
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        mEditor.insertImage(m_Text,
                                m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddYoutubeActivity.this);
                //you should edit this to fit your needs
                builder.setTitle("Insert Link");

                final EditText one = new EditText(AddYoutubeActivity.this);
                final EditText two = new EditText(AddYoutubeActivity.this);

                one.setHint("Text");
                two.setHint("URL");

                LinearLayout lay = new LinearLayout(AddYoutubeActivity.this);
                lay.setOrientation(LinearLayout.VERTICAL);
                lay.addView(one);
                lay.addView(two);
                builder.setView(lay);

                // Set up the buttons
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //get the two inputs
                        String i = one.getText().toString();
                        String j = two.getText().toString();
                        mEditor.insertLink(j,i);

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                builder.show();


            }
        });


        gridLayoutManagerCategorySelect = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);

        recycle_view_selected_category= (RecyclerView) findViewById(R.id.recycle_view_selected_category);

        spinner_language_select = (Spinner) findViewById(R.id.spinner_language_select);


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.back_enter, R.anim.back_exit);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int iditem = item.getItemId();

        switch (iditem){
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.back_enter, R.anim.back_exit);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean save(){
        if (edit_text_upload_title.getText().toString().trim().length()<3){
            Toasty.error(AddYoutubeActivity.this, getResources().getString(R.string.edit_text_upload_title_error), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!isYoutubeUrl(edit_text_upload_youtube.getText().toString().trim())){
            Toasty.error(AddYoutubeActivity.this, getResources().getString(R.string.edit_text_upload_youtube_error), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (edit_text_upload_description.getHtml() != null){
            if (edit_text_upload_description.getHtml().toString().trim().length()<10){
                Toasty.error(AddYoutubeActivity.this, getResources().getString(R.string.edit_text_upload_description_error), Toast.LENGTH_SHORT).show();
                return true;
            }
        }else{
            Toasty.error(AddYoutubeActivity.this, getResources().getString(R.string.edit_text_upload_description_error), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (imageUrl==null){
            Toasty.error(AddYoutubeActivity.this, getResources().getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show();
            return true;
        }

        upload();
        return true;
    }
    private void initAction() {
        image_view_post_activity_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();

            }
        });

        spinner_language_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getCategory();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        image_view_guide_activity_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        image_view_done_rich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = edit_text_upload_description.getHtml();
                relativeLayout_rich_box.setVisibility(View.GONE);
                web_view_guide_activity_content.loadData(content,"text/html; charset=utf-8", "utf-8");
                web_view_guide_activity_content.setVisibility(View.VISIBLE);
                text_view_show_rish.setVisibility(View.GONE);
            }
        });
        text_view_show_rish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout_rich_box.setVisibility(View.VISIBLE);
                edit_text_upload_description.setHtml( content);
            }
        });
        image_view_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout_rich_box.setVisibility(View.VISIBLE);
                edit_text_upload_description.setHtml( content);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && null != data) {


            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            imageUrl = picturePath  ;
            File file = new File(imageUrl);
            Log.v("SIZE",file.getName()+"");
            image_view_add_guide_preview.setImageURI(selectedImage);

        } else {

            Log.i("SonaSys", "resultCode: " + resultCode);
            switch (resultCode) {
                case 0:
                    Log.i("SonaSys", "User cancelled");
                    break;
                case -1:
                    break;
            }
        }
    }
    public String getSelectedCategories(){
        String categories = "";
        for (int i = 0; i < categorySelectAdapter.getSelectedItems().size(); i++) {
            categories+="_"+categorySelectAdapter.getSelectedItems().get(i).getId();
        }
        Log.v("categories",categories);

        return categories;
    }

    private void getCategory() {
        register_progress.show();

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        PrefManager prefManager= new PrefManager(getApplicationContext());

        String language=languageList.get(spinner_language_select.getSelectedItemPosition()).getId().toString();
        Call<List<Category>> call = service.allCategoriesByLanguage(language);
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                register_progress.dismiss();
                if(response.isSuccessful()){
                    categoriesListObj.clear();
                    for (int i = 0;i<response.body().size();i++){

                        categoriesListObj.add(response.body().get(i));
                    }
                    categorySelectAdapter = new CategorySelectAdapter(AddYoutubeActivity.this, categoriesListObj, true, AddYoutubeActivity.this);
                    recycle_view_selected_category.setHasFixedSize(true);
                    recycle_view_selected_category.setAdapter(categorySelectAdapter);
                    recycle_view_selected_category.setLayoutManager(gridLayoutManagerCategorySelect);
                    if (response.body().size()>0) {
                        linear_layout_categories.setVisibility(View.VISIBLE);
                    }

                }else {
                    Snackbar snackbar = Snackbar
                            .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getCategory();
                                }
                            });
                    snackbar.setActionTextColor(android.graphics.Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(android.graphics.Color.YELLOW);
                    snackbar.show();
                }

            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                register_progress.dismiss();
                Snackbar snackbar = Snackbar
                        .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getCategory();
                            }
                        });
                snackbar.setActionTextColor(android.graphics.Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(android.graphics.Color.YELLOW);
                snackbar.show();
            }
        });
    }
    private void getLanguages(){
        register_progress.show();

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Language>> call = service.languageAll();
        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {
                if(response.isSuccessful()) {
                    languageList.clear();
                    ArrayList<String> contactlist= new ArrayList<String>();

                    for (int i = 0; i < response.body().size(); i++) {
                        languageList.add(response.body().get(i));
                        contactlist.add(response.body().get(i).getLanguage());
                    }
                    ArrayAdapter adapter = new ArrayAdapter(AddYoutubeActivity.this, R.layout.my_spinner_dropdown_item, contactlist);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_language_select.setAdapter(adapter);

                    PrefManager prefManager= new PrefManager(getApplicationContext());
                    String selected_language=prefManager.getString("LANGUAGE_DEFAULT");

                    for (int i = 0; i < languageList.size(); i++) {
                        if(languageList.get(i).getId() ==  Integer.parseInt(selected_language)){
                            spinner_language_select.setSelection(i);
                        }
                    }




                    //fab_save_upload.show();
                }else{
                    Snackbar snackbar = Snackbar
                            .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getLanguages();
                                }
                            });
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                }
                register_progress.show();

            }
            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                register_progress.dismiss();
                Snackbar snackbar = Snackbar
                        .make(relative_layout_upload, getResources().getString(R.string.no_connexion), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getResources().getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getLanguages();
                            }
                        });
                snackbar.setActionTextColor(android.graphics.Color.RED);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(android.graphics.Color.YELLOW);
                snackbar.show();
            }
        });
    }
    @Override
    public void onItemSelected(Language item) {

    }

    @Override
    public void onItemSelected(Category item) {

    }
    @Override
    public void onProgressUpdate(int percentage) {
        pd.setProgress(percentage);
    }

    @Override
    public void onError() {
        pd.dismiss();
        pd.cancel();
    }

    @Override
    public void onFinish() {
        pd.dismiss();
        pd.cancel();

    }
    public void upload(){
        File file1 = new File(imageUrl);
        int file_size = Integer.parseInt(String.valueOf(file1.length()/1024/1024));
        if (file_size>20){
            Toasty.error(getApplicationContext(),"Max file size allowed 20M",Toast.LENGTH_LONG).show();
        }
        Log.v("SIZE",file1.getName()+"");


        pd.show();
        PrefManager prf = new PrefManager(getApplicationContext());

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);

        //File creating from selected URL
        final File file = new File(imageUrl);


        ProgressRequestBody requestFile = new ProgressRequestBody(file, AddYoutubeActivity.this);

        // create RequestBody instance from file
        // RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
        String id_ser=  prf.getString("ID_USER");
        String key_ser=  prf.getString("TOKEN_USER");

        Call<ApiResponse> request = service.uploadYoutube(body, id_ser, key_ser,edit_text_upload_youtube.getText().toString(), edit_text_upload_title.getText().toString().trim(),edit_text_upload_description.getHtml().toString().trim(),languageList.get(spinner_language_select.getSelectedItemPosition()).getId().toString(),getSelectedCategories());

        request.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful()){
                    Toasty.success(getApplication(),getResources().getString(R.string.post_upload_success),Toast.LENGTH_LONG).show();
                    Integer id=0;
                    String title="";
                    for (int i = 0; i < response.body().getValues().size(); i++) {
                        if (response.body().getValues().get(i).getName().equals("id")){
                            id = Integer.parseInt(response.body().getValues().get(i).getValue());
                        }
                        if (response.body().getValues().get(i).getName().equals("title")){
                            title = response.body().getValues().get(i).getValue();
                        }
                    }

                    Intent intent  =  new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("id",id);
                    intent.putExtra("title",title);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    finish();
                }else{
                    Toasty.error(getApplication(),getResources().getString(R.string.no_connexion),Toast.LENGTH_LONG).show();

                }
                // file.delete();
                // getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                pd.dismiss();
                pd.cancel();
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toasty.error(getApplication(),getResources().getString(R.string.no_connexion),Toast.LENGTH_LONG).show();
                pd.dismiss();
                pd.cancel();
            }
        });
    }
    public static boolean isYoutubeUrl(String youTubeURl)
    {
        boolean success;
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        if (!youTubeURl.isEmpty() && youTubeURl.matches(pattern))
        {
            success = true;
        }
        else
        {
            // Not Valid youtube URL
            success = false;
        }
        return success;
    }
}
