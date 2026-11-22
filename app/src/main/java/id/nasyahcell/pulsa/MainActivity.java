package id.nasyahcell.pulsa;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private EditText phoneInput;
    private Spinner nominalSpinner;
    private TextView statusText;
    private int blue = Color.rgb(22, 103, 190);

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.rgb(15, 76, 145));
        getWindow().setNavigationBarColor(Color.rgb(15, 76, 145));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(245, 248, 252));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(28));
        scroll.addView(page);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(22), dp(20), dp(22));
        header.setBackground(round(blue, 20));
        TextView brand = text("NASYAH CELL", 23, Color.WHITE, true);
        TextView subtitle = text("Layanan isi pulsa", 14, Color.WHITE, false);
        header.addView(brand);
        header.addView(subtitle);
        page.addView(header, matchWrap());

        TextView title = text("Isi Pulsa", 22, Color.rgb(30, 41, 59), true);
        LinearLayout.LayoutParams titleLp = matchWrap();
        titleLp.topMargin = dp(24);
        page.addView(title, titleLp);
        page.addView(text("Masukkan nomor tujuan dan pilih nominal pulsa.", 13,
                Color.rgb(100, 116, 139), false), matchWrap());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, 18));
        LinearLayout.LayoutParams cardLp = matchWrap();
        cardLp.topMargin = dp(18);
        page.addView(card, cardLp);

        card.addView(label("Nomor HP tujuan"));
        phoneInput = new EditText(this);
        phoneInput.setSingleLine(true);
        phoneInput.setInputType(3);
        phoneInput.setHint("Contoh: 081234567890");
        phoneInput.setTextSize(16);
        card.addView(phoneInput, matchWrap());

        LinearLayout.LayoutParams gap = matchWrap();
        gap.topMargin = dp(16);
        TextView nominalLabel = label("Nominal pulsa");
        nominalLabel.setLayoutParams(gap);
        card.addView(nominalLabel);

        nominalSpinner = new Spinner(this);
        String[] values = {"Pilih nominal", "Pulsa 5.000", "Pulsa 10.000", "Pulsa 15.000",
                "Pulsa 20.000", "Pulsa 25.000", "Pulsa 50.000", "Pulsa 100.000"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, values);
        nominalSpinner.setAdapter(adapter);
        card.addView(nominalSpinner, matchWrap());

        Button buy = new Button(this);
        buy.setText("LANJUTKAN");
        buy.setTextColor(Color.WHITE);
        buy.setAllCaps(false);
        buy.setTextSize(16);
        buy.setBackground(round(blue, 14));
        LinearLayout.LayoutParams buyLp = matchWrap();
        buyLp.topMargin = dp(22);
        card.addView(buy, buyLp);

        statusText = text("Mode tampilan: transaksi belum dikirim ke server.", 13,
                Color.rgb(100, 116, 139), false);
        LinearLayout.LayoutParams statusLp = matchWrap();
        statusLp.topMargin = dp(16);
        card.addView(statusText, statusLp);

        TextView info = text("Catatan: versi standar ini adalah prototipe antarmuka. "
                + "Tombol belum mengirim transaksi ke OtomaX. Integrasi akan diaktifkan "
                + "setelah alamat API resmi, autentikasi, dan format request diverifikasi.",
                12, Color.rgb(100, 116, 139), false);
        LinearLayout.LayoutParams infoLp = matchWrap();
        infoLp.topMargin = dp(18);
        page.addView(info, infoLp);

        TextView footer = text("© Nasyah Cell", 12, Color.rgb(148, 163, 184), false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerLp = matchWrap();
        footerLp.topMargin = dp(26);
        page.addView(footer, footerLp);

        buy.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            int selected = nominalSpinner.getSelectedItemPosition();
            if (phone.length() < 10 || phone.length() > 15) {
                phoneInput.setError("Periksa nomor HP (10–15 digit).");
                return;
            }
            if (selected == 0) {
                statusText.setText("Pilih nominal pulsa terlebih dahulu.");
                return;
            }
            statusText.setText("Data siap diuji: " + phone + " • "
                    + nominalSpinner.getSelectedItem().toString()
                    + ". Belum ada transaksi yang dikirim.");
        });

        setContentView(scroll);
    }

    private TextView label(String s) {
        return text(s, 14, Color.rgb(51, 65, 85), true);
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }
    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
