package id.nasyahcell.pulsa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int RED = Color.rgb(218, 43, 48);
    private static final int DARK = Color.rgb(5, 62, 78);
    private static final int BG = Color.rgb(245, 246, 248);
    private static final String SERVER_URL = "https://charming-antarctica-cat-gmbh.trycloudflare.com";
    // Ganti nilai ini dengan API key dari config.json di PC. Jangan kirim key ke orang lain.
    private static final String API_KEY = "NasyahCell-2026-KunciRahasia-73921";
    private static final int REQUEST_CONTACT = 1001;

    private LinearLayout root, productList;
    private TextView saldoText, pageStatus;
    private EditText phoneInput;
    private final ArrayList<JSONObject> products = new ArrayList<>();
    private String selectedCategory = "";
    private JSONObject selectedProduct;
    private ArrayList<JSONObject> categoryProducts = new ArrayList<>();
    private boolean renderingProducts = false;
    private double saldoValue = 0;
    private final String[] categories = {"Pulsa", "Paket Data", "Token PLN", "Telepon & SMS", "Voucher Data", "Masa Aktif", "Paket Terbaik", "Game", "Kirim Uang", "Kartu Perdana", "Layanan Digital"};
    private final String[] icons = {"📱", "🌐", "💡", "☎", "🎟", "🗓", "🏷", "🎮", "💸", "💳", "🛒"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showHome();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD); return t;
    }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); }
    private String rupiah(double n) { return "Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(n); }
    private void message(String title, String msg) { new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK", null).show(); }

    private void showHome() {
        selectedCategory = "";
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(16),dp(14),dp(16),dp(14)); header.setBackgroundColor(RED);
        TextView brand = tv("NASYAH CELL",21,Color.WHITE,true); header.addView(brand,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView refresh = tv("⟳",30,Color.WHITE,true); refresh.setGravity(Gravity.CENTER); header.addView(refresh,lp(45,45)); refresh.setOnClickListener(v->loadSaldo()); root.addView(header);
        TextView tab = tv("PRABAYAR     PEMBAYARAN     MENU",13,Color.WHITE,true); tab.setGravity(Gravity.CENTER); tab.setPadding(0,dp(16),0,dp(16)); tab.setBackgroundColor(DARK); root.addView(tab,lp(-1,-2));
        ScrollView scroll = new ScrollView(this); LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(16),dp(14),dp(24)); scroll.addView(body);
        LinearLayout promo = new LinearLayout(this); promo.setGravity(Gravity.CENTER_VERTICAL); promo.setPadding(dp(18),dp(18),dp(18),dp(18)); promo.setBackground(bg(RED,16));
        LinearLayout promoTxt = new LinearLayout(this); promoTxt.setOrientation(LinearLayout.VERTICAL); TextView p1=tv("Produk Nasyah Cell",21,Color.WHITE,true); TextView p2=tv("Pulsa, paket data dan PPOB",13,Color.WHITE,false); promoTxt.addView(p1); promoTxt.addView(p2); promo.addView(promoTxt,new LinearLayout.LayoutParams(0,-2,1)); promo.addView(tv("📱",42,Color.WHITE,true)); body.addView(promo,lp(-1,110));
        LinearLayout balance = new LinearLayout(this); balance.setOrientation(LinearLayout.VERTICAL); balance.setPadding(dp(16),dp(14),dp(16),dp(14)); balance.setBackground(bg(Color.WHITE,14)); LinearLayout.LayoutParams blp=lp(-1,-2); blp.topMargin=dp(16); body.addView(balance,blp);
        TextView btitle=tv("SALDO ANDA",14,Color.GRAY,true); balance.addView(btitle); saldoText=tv("Memuat saldo...",22,Color.BLACK,true); LinearLayout.LayoutParams slp=lp(-1,-2); slp.topMargin=dp(6); balance.addView(saldoText,slp);
        TextView section=tv("Pilih Layanan",19,Color.BLACK,true); LinearLayout.LayoutParams stp=lp(-1,-2); stp.topMargin=dp(22); body.addView(section,stp);
        for(int i=0;i<categories.length;i+=3){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.TOP); for(int j=i;j<Math.min(i+3,categories.length);j++) addCategory(row,j); LinearLayout.LayoutParams rlp=lp(-1,-2); rlp.topMargin=dp(15); body.addView(row,rlp); }
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); TextView bottom=tv("Kontak CS          Riwayat          QRIS          Akun",12,Color.DKGRAY,true); bottom.setGravity(Gravity.CENTER); bottom.setBackgroundColor(Color.WHITE); bottom.setPadding(0,dp(15),0,dp(15)); root.addView(bottom,lp(-1,-2)); setContentView(root); loadSaldo();
    }
    private void addCategory(LinearLayout row,int i){
        LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(dp(4),dp(8),dp(4),dp(8));
        TextView ico=tv(icons[i],30,RED,true); ico.setGravity(Gravity.CENTER); ico.setBackground(bg(Color.WHITE,16)); cell.addView(ico,lp(72,68)); TextView name=tv(categories[i],12,Color.BLACK,true); name.setGravity(Gravity.CENTER); name.setMaxLines(2); LinearLayout.LayoutParams nlp=lp(-1,-2); nlp.topMargin=dp(7); cell.addView(name,nlp); row.addView(cell,new LinearLayout.LayoutParams(0,-2,1)); cell.setOnClickListener(v->showProducts(categories[i]));
    }

    private void loadSaldo(){ new Thread(()->{ HttpURLConnection c=null; try{ JSONObject j=getJson(SERVER_URL+"/api/saldo"); double s=j.optDouble("saldo",0); runOnUiThread(()->{saldoValue=s;if(saldoText!=null)saldoText.setText(rupiah(s));}); }catch(Exception e){runOnUiThread(()->{if(saldoText!=null)saldoText.setText("Saldo tidak dapat dimuat");});} }).start(); }

    private JSONObject getJson(String address) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();
        try { c.setRequestMethod("GET"); c.setConnectTimeout(15000); c.setReadTimeout(20000); c.setRequestProperty("X-API-Key",API_KEY);
            int code=c.getResponseCode(); InputStream in=(code>=200&&code<300)?c.getInputStream():c.getErrorStream();
            if(in==null) throw new Exception("HTTP "+code); BufferedReader r=new BufferedReader(new InputStreamReader(in,"UTF-8")); StringBuilder b=new StringBuilder(); String line; while((line=r.readLine())!=null)b.append(line); r.close(); if(code!=200)throw new Exception("HTTP "+code+" "+b); return new JSONObject(b.toString());
        } finally { c.disconnect(); }
    }

    private void showProducts(String category){
        selectedCategory=category; selectedProduct=null;
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(12),dp(8),dp(12),dp(8)); header.setBackgroundColor(Color.WHITE);
        TextView back=tv("‹",36,RED,true); back.setGravity(Gravity.CENTER); header.addView(back,lp(42,48)); TextView title=tv(category,20,Color.BLACK,true); header.addView(title); page.addView(header); back.setOnClickListener(v->showHome());
        ScrollView scroll=new ScrollView(this); LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(16),dp(14),dp(24)); scroll.addView(body);
        LinearLayout numberCard=new LinearLayout(this); numberCard.setOrientation(LinearLayout.VERTICAL); numberCard.setPadding(dp(14),dp(14),dp(14),dp(14)); numberCard.setBackground(bg(Color.WHITE,14)); body.addView(numberCard,lp(-1,-2)); numberCard.addView(tv("Nomor Tujuan",14,Color.BLACK,true));
        LinearLayout nr=new LinearLayout(this); nr.setGravity(Gravity.CENTER_VERTICAL); LinearLayout.LayoutParams nrp=lp(-1,52); nrp.topMargin=dp(8); numberCard.addView(nr,nrp);
        phoneInput=new EditText(this); phoneInput.setSingleLine(true); phoneInput.setTextSize(15); phoneInput.setHint("Masukkan nomor tujuan"); phoneInput.setInputType(3); phoneInput.setPadding(dp(10),0,dp(10),0); phoneInput.setBackground(bg(BG,9)); nr.addView(phoneInput,new LinearLayout.LayoutParams(0,-1,1));
        TextView contact=tv("Kontak",13,Color.WHITE,true); contact.setGravity(Gravity.CENTER); contact.setBackground(bg(RED,9)); LinearLayout.LayoutParams clp=lp(82,-1); clp.leftMargin=dp(8); nr.addView(contact,clp); contact.setOnClickListener(v->pickContact());
        phoneInput.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count){ if(!renderingProducts) filterProductsByNumber(); }
            @Override public void afterTextChanged(Editable e){}
        });
        TextView choose=tv("Pilih Nominal / Produk",17,Color.BLACK,true); LinearLayout.LayoutParams chp=lp(-1,-2); chp.topMargin=dp(20); body.addView(choose,chp);
        pageStatus=tv("Memuat produk dari OtomaX...",12,Color.GRAY,false); LinearLayout.LayoutParams psp=lp(-1,-2); psp.topMargin=dp(6); body.addView(pageStatus,psp);
        productList=new LinearLayout(this); productList.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams plp=lp(-1,-2); plp.topMargin=dp(10); body.addView(productList,plp);
        root=page; page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(page); loadProducts(category);
    }

    private void loadProducts(String category){
        pageStatus.setText("Masukkan nomor tujuan untuk melihat produk yang sesuai.");
        productList.removeAllViews();
        new Thread(()->{ try{
            JSONObject response=getJson(SERVER_URL+"/api/produk?kategori="+java.net.URLEncoder.encode(category,"UTF-8"));
            JSONArray arr=response.optJSONArray("produk"); if(arr==null)throw new Exception("Daftar produk tidak ditemukan.");
            ArrayList<JSONObject> found=new ArrayList<>();
            for(int i=0;i<arr.length();i++){JSONObject item=arr.optJSONObject(i);if(item!=null)found.add(item);}
            runOnUiThread(()->{categoryProducts=found;filterProductsByNumber();});
        }catch(Exception e){runOnUiThread(()->{if(pageStatus!=null)pageStatus.setText("Gagal memuat produk: "+e.getMessage());});}}).start();
    }

    // Deteksi operator berdasarkan awalan nomor Indonesia. Produk hanya ditampilkan
    // jika nama operator dari database OtomaX cocok, agar produk operator lain tidak salah dipilih.
    private String detectOperator(String raw){
        String n=raw.replaceAll("[^0-9]","");
        if(n.startsWith("62")) n="0"+n.substring(2);
        if(n.startsWith("8")) n="0"+n;
        if(n.length()<4) return "";
        String p4=n.substring(0,4);
        String p3=n.substring(0,3);
        if(p4.matches("0856|0857|0858|0814|0815|0816")) return "INDOSAT";
        if(p4.matches("0811|0812|0813|0821|0822|0823|0852|0853|0851")) return "TELKOMSEL";
        if(p4.matches("0817|0818|0819|0859|0877|0878")) return "XL";
        if(p4.matches("0831|0832|0833|0838")) return "AXIS";
        if(p4.matches("0895|0896|0897|0898|0899")) return "TRI";
        if(p4.matches("0881|0882|0883|0884|0885|0886|0887|0888|0889")) return "SMARTFREN";
        if(p3.equals("085")) return "";
        return "";
    }

    private boolean providerMatches(JSONObject p,String operator){
        String provider=(p.optString("nama_provider","")+" "+p.optString("nama_produk","")).toUpperCase(Locale.ROOT);
        if(operator.equals("INDOSAT")) return provider.contains("INDOSAT")||provider.contains("IM3")||provider.contains("MENTARI")||provider.contains("MATRIX");
        if(operator.equals("TELKOMSEL")) return provider.contains("TELKOMSEL")||provider.contains("SIMPATI")||provider.contains("AS ")||provider.contains("BY.U")||provider.contains("BYU");
        if(operator.equals("XL")) return provider.contains("XL")||provider.contains("XL AXIATA");
        if(operator.equals("AXIS")) return provider.contains("AXIS");
        if(operator.equals("TRI")) return provider.contains("TRI")||provider.contains("THREE");
        if(operator.equals("SMARTFREN")) return provider.contains("SMARTFREN");
        return false;
    }

    private void filterProductsByNumber(){
        if(productList==null||pageStatus==null||phoneInput==null)return;
        String number=phoneInput.getText().toString().trim();
        if(number.isEmpty()){
            productList.removeAllViews();
            pageStatus.setText("Masukkan nomor tujuan untuk melihat produk yang sesuai.");
            return;
        }
        String operator=detectOperator(number);
        if(operator.isEmpty()){
            productList.removeAllViews();
            pageStatus.setText("Operator belum dikenali. Periksa nomor tujuan.");
            return;
        }
        ArrayList<JSONObject> matched=new ArrayList<>();
        for(JSONObject p:categoryProducts) if(providerMatches(p,operator)) matched.add(p);
        renderProducts(matched,selectedCategory,operator);
    }

    private void renderProducts(ArrayList<JSONObject> found,String category,String operator){
        if(productList==null||pageStatus==null)return; products.clear();products.addAll(found);productList.removeAllViews();
        pageStatus.setText(operator+" • "+found.size()+" produk tersedia dari OtomaX");
        if(found.isEmpty()){
            TextView empty=tv("Belum ada produk "+operator+" yang cocok di data OtomaX. Produk operator lain disembunyikan agar tidak salah transaksi.",14,Color.GRAY,false);
            empty.setPadding(dp(4),dp(8),dp(4),dp(8)); productList.addView(empty);return;
        }
        for(JSONObject p:found){
            boolean open=p.optString("status_produk","OPEN").equalsIgnoreCase("OPEN");
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(15),dp(13),dp(15),dp(13));card.setBackground(bg(Color.WHITE,13));
            LinearLayout.LayoutParams cardLp=lp(-1,-2);cardLp.bottomMargin=dp(9);productList.addView(card,cardLp);
            LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);card.addView(top);
            String code=p.optString("kode_produk",""); String nominal=p.optString("nominal","");
            TextView codeView=tv(code+(nominal.isEmpty()?"":"  •  "+nominal),16,Color.BLACK,true);top.addView(codeView,new LinearLayout.LayoutParams(0,-2,1));
            TextView state=tv(open?"OPEN":"GANGGUAN",11,Color.WHITE,true);state.setGravity(Gravity.CENTER);state.setPadding(dp(9),dp(5),dp(9),dp(5));state.setBackground(bg(open?Color.rgb(35,150,85):Color.rgb(210,75,55),20));top.addView(state);
            String name=p.optString("nama_produk",""); if(!name.isEmpty()){TextView nm=tv(name,12,Color.GRAY,false);LinearLayout.LayoutParams nmp=lp(-1,-2);nmp.topMargin=dp(5);card.addView(nm,nmp);}
            TextView price=tv(rupiah(p.optDouble("harga_jual",0)),16,RED,true);LinearLayout.LayoutParams prp=lp(-1,-2);prp.topMargin=dp(6);card.addView(price,prp);
            if(open){card.setOnClickListener(v->{selectedProduct=p;confirmProduct(p);});}else{card.setAlpha(0.65f);}
        }
    }
    private void confirmProduct(JSONObject p){
        String n=phoneInput.getText().toString().trim(); if(n.isEmpty()){message("Nomor tujuan","Masukkan nomor tujuan terlebih dahulu.");return;}
        String detail="Nomor: "+n+"\nProduk: "+p.optString("kode_produk")+"\nHarga: "+rupiah(p.optDouble("harga_jual",0))+"\n\nFitur pengiriman transaksi ke OtomaX belum diaktifkan. Ini hanya pratinjau pilihan produk.";
        new AlertDialog.Builder(this).setTitle("Konfirmasi Produk").setMessage(detail).setNegativeButton("Kembali",null).setPositiveButton("OK",(d,w)->{}).show();
    }
    private void pickContact(){try{Intent i=new Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI);startActivityForResult(i,REQUEST_CONTACT);}catch(Exception e){message("Kontak","Tidak dapat membuka kontak.");}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==REQUEST_CONTACT&&result==RESULT_OK&&data!=null){Cursor c=null;try{c=getContentResolver().query(data.getData(),new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,null);if(c!=null&&c.moveToFirst()&&phoneInput!=null)phoneInput.setText(c.getString(0));}catch(Exception e){message("Kontak","Gagal mengambil nomor.");}finally{if(c!=null)c.close();}}}
    @Override public void onBackPressed(){if(!selectedCategory.isEmpty())showHome();else new AlertDialog.Builder(this).setTitle("Keluar Aplikasi").setMessage("Yakin ingin keluar dari NASYAH CELL?").setNegativeButton("Tidak",null).setPositiveButton("Ya",(d,w)->finish()).show();}
}
