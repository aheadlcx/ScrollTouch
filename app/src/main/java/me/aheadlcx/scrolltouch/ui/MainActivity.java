package me.aheadlcx.scrolltouch.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.aheadlcx.scrolltouch.R;
import me.aheadlcx.scrolltouch.model.Star;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtAI;
    private TextView txtKB;
    private TextView txtLBJ;
    private TextView txtKD;
    private TextView txtTMAC;
    private TextView txtWD;
    private ItemDetailFrag lastFrag;
    private String TAG_Frag = "TAG_Frag";
    private int current = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    private void initListeners() {
        txtKD.setOnClickListener(this);
        txtLBJ.setOnClickListener(this);
        txtKB.setOnClickListener(this);
        txtAI.setOnClickListener(this);
        txtTMAC.setOnClickListener(this);
        txtWD.setOnClickListener(this);
    }

    private void initViews() {
        this.txtKD = (TextView) findViewById(R.id.txtKD);
        this.txtLBJ = (TextView) findViewById(R.id.txtLBJ);
        this.txtKB = (TextView) findViewById(R.id.txtKB);
        this.txtAI = (TextView) findViewById(R.id.txtAI);
        this.txtWD = (TextView) findViewById(R.id.txtWD);
        this.txtTMAC = (TextView) findViewById(R.id.txtTMAC);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtAI:
                showFrag(0, "Allen Iverson", R.drawable.ai);
                break;
            case R.id.txtKB:
                showFrag(1, "Kobe Bryant", R.drawable.kb);
                break;
            case R.id.txtLBJ:
                showFrag(2, "LBJ", R.drawable.lbj);
                break;
            case R.id.txtKD:
                showFrag(3, "KD", R.drawable.kd);
                break;
            case R.id.txtTMAC:
                Toast.makeText(MainActivity.this, "T_MAC", Toast.LENGTH_SHORT).show();
                break;
            case R.id.txtWD:
                Toast.makeText(MainActivity.this, "WADER", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void showFrag(int tabInt, String name , int resid) {
        if (current == tabInt && findFrag() != null) {
            Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
            return;
        }
        current = tabInt;
        hideLast();
        lastFrag = new ItemDetailFrag();
        Bundle bundle = new Bundle();
        Star star = new Star(resid).setName(name);
        bundle.putParcelable(ItemDetailFrag.star, star);
        lastFrag.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_detail, lastFrag, TAG_Frag).addToBackStack(null).commit();
    }

    private void hideLast() {
        if (lastFrag != null) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public ItemDetailFrag findFrag() {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(TAG_Frag);
        return (ItemDetailFrag) fragmentByTag;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
