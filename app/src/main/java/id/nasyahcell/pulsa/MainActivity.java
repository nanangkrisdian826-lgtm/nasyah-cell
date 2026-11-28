
package id.nasyahcell.pulsa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {

    private final int RED = Color.rgb(218, 43, 48);
    private final int DARK = Color.rgb(5, 62, 78);
    private final int BG = Color.rgb(245, 246, 248);
    private final int BLUE = Color.rgb(20, 105, 180);
    private final int PANEL = Color.rgb(134, 164, 173);

    private LinearLayout root;
    private LinearLayout content;
    private TextView balanceText;
    private TextView saldoAmountText;
    private double saldoOtomax = 0;

    // Sesuaikan URL ini jika alamat Cloudflare Tunnel sudah berubah.
    private static final String SERVER_URL =
            "https://responses-neil-property-viii.trycloudflare.com";

    // Isi dengan API key yang sama persis seperti di config.json server.
    private static final String API_KEY = "NasyahCell-2026-KunciRahasia-73921";

    private static final String PREFS = "nasyah_auth";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_KODE = "kode_reseller";
    private String tokenLogin = "";

    private EditText numberInput;
    private LinearLayout productList;
    private JSONObject selectedProduct;
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;
    private TextView statusText;
    private TextView providerNameText;
    private final ArrayList<JSONObject> daftarProduk = new ArrayList<>();
    private final ArrayList<JSONObject> daftarProvider = new ArrayList<>();
    private String selectedProviderCode = null;

    private boolean saldoVisible = false;
    private boolean depositScreen = false;
    private boolean accountScreen = false;
    private String selectedCategory = null;

    private static final int REQUEST_CONTACT = 1001;

    private final String[] categories = {
            "Pulsa", "Paket Data", "Token PLN", "Telepon & SMS",
            "Voucher Data", "Masa Aktif", "Paket Terbaik", "Game",
            "Kirim Uang", "Kartu Perdana", "Layanan Digital"
    };

    private final String[] icons = {
            "📱", "🌐", "💡", "☎️",
            "🎟️", "🗓️", "🏷️", "🎮",
            "💸", "💳", "🛒"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(RED);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        tokenLogin = prefs.getString(PREF_TOKEN, "");
        if (tokenLogin.isEmpty()) {
            showLogin();
        } else {
            cekSesiLaluMasuk();
        }
    }

    private void showLogin() {
        accountScreen = false;
        depositScreen = false;
        selectedCategory = null;

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(24), dp(20), dp(24), dp(20));
        page.setBackgroundColor(Color.WHITE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(18), dp(25), dp(18), dp(25));
        card.setBackground(round(PANEL, 20));

        TextView title = text("LOGIN", 28, DARK, true);
        title.setGravity(Gravity.CENTER);
        card.addView(title, wrap());

        TextView logo = text("NASYAH\nCELL", 25, RED, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(Color.WHITE, 60));
        LinearLayout.LayoutParams logoLp =
                new LinearLayout.LayoutParams(dp(110), dp(110));
        logoLp.gravity = Gravity.CENTER;
        logoLp.topMargin = dp(25);
        logoLp.bottomMargin = dp(35);
        card.addView(logo, logoLp);

        EditText kode = authInput("Kode Reseller");
        card.addView(kode, authFieldLp());

        EditText password = authInput("Password Nasyah Cell");
        password.setInputType(129);
        card.addView(password, authFieldLp());

        TextView masuk = button("MASUK");
        LinearLayout.LayoutParams masukLp = wrap();
        masukLp.topMargin = dp(15);
        card.addView(masuk, masukLp);
        masuk.setOnClickListener(v -> prosesAuth(
                "/api/login",
                kode.getText().toString().trim(),
                password.getText().toString(),
                "",
                ""
        ));

        TextView daftar = text(
                "Belum punya akun? DAFTAR",
                14, BLUE, true
        );
        daftar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams daftarLp = wrap();
        daftarLp.topMargin = dp(22);
        card.addView(daftar, daftarLp);
        daftar.setOnClickListener(v -> showRegister());

        TextView footer = text("NASYAH CELL", 16, DARK, true);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerLp = wrap();
        footerLp.topMargin = dp(30);
        card.addView(footer, footerLp);

        scroll.addView(card, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(page);
    }

    private void showRegister() {
        accountScreen = false;
        depositScreen = false;
        selectedCategory = null;

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(dp(20), dp(18), dp(20), dp(18));
        page.setBackgroundColor(Color.WHITE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(18), dp(25), dp(18), dp(22));
        card.setMinimumHeight(dp(610));
        card.setBackground(round(PANEL, 22));

        TextView title = text("PENDAFTARAN", 27, DARK, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = wrap();
        titleLp.bottomMargin = dp(30);
        card.addView(title, titleLp);

        EditText nama = authInput("Nama");
        nama.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        card.addView(nama, authFieldLp());

        EditText alamat = authInput("Alamat");
        alamat.setSingleLine(true);
        card.addView(alamat, authFieldLp());

        EditText hp = authInput("No HP");
        hp.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        card.addView(hp, authFieldLp());

        EditText referral = authInput("Kode Referral");
        referral.setSingleLine(true);
        card.addView(referral, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        TextView optional = text("Tidak wajib diisi.", 12, DARK, false);
        LinearLayout.LayoutParams optionalLp = wrap();
        optionalLp.leftMargin = dp(5);
        optionalLp.topMargin = dp(4);
        optionalLp.bottomMargin = dp(15);
        card.addView(optional, optionalLp);

        CheckBox setuju = new CheckBox(this);
        setuju.setButtonTintList(android.content.res.ColorStateList.valueOf(DARK));
        setuju.setText("Dengan mendaftar, Anda dianggap telah setuju dengan syarat dan ketentuan yang berlaku.");
        setuju.setTextColor(DARK);
        setuju.setTextSize(13);
        setuju.setPadding(dp(4), dp(2), dp(2), dp(2));
        card.addView(setuju, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView kirim = button("KIRIM");
        LinearLayout.LayoutParams kirimLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        kirimLp.topMargin = dp(20);
        card.addView(kirim, kirimLp);
        kirim.setOnClickListener(v -> {
            String namaText = nama.getText().toString().trim();
            String alamatText = alamat.getText().toString().trim();
            String hpText = hp.getText().toString().trim();
            String referralText = referral.getText().toString().trim();
            if (namaText.isEmpty() || alamatText.isEmpty() || hpText.isEmpty()) {
                showMessage("Pendaftaran", "Nama, alamat, dan nomor HP wajib diisi.");
                return;
            }
            if (hpText.replaceAll("[^0-9]", "").length() < 9) {
                showMessage("Pendaftaran", "Nomor HP belum valid.");
                return;
            }
            if (!setuju.isChecked()) {
                showMessage("Persetujuan", "Centang syarat dan ketentuan terlebih dahulu.");
                return;
            }
            kirimPermohonan(namaText, alamatText, hpText, referralText);
        });

        TextView kembali = text("←  KEMBALI", 15, RED, true);
        kembali.setGravity(Gravity.CENTER);
        kembali.setPadding(dp(15), dp(14), dp(15), dp(14));
        kembali.setBackground(round(Color.WHITE, 8));
        LinearLayout.LayoutParams kembaliLp = new LinearLayout.LayoutParams(dp(150), dp(58));
        kembaliLp.gravity = Gravity.CENTER;
        kembaliLp.topMargin = dp(18);
        card.addView(kembali, kembaliLp);
        kembali.setOnClickListener(v -> showLogin());

        TextView footer = text("NASYAH CELL", 16, DARK, true);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerLp = wrap();
        footerLp.topMargin = dp(24);
        card.addView(footer, footerLp);

        scroll.addView(card, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(page);
    }

    private void kirimPermohonan(String nama, String alamat, String hp, String referral) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + "/api/permohonan");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("X-API-Key", API_KEY);

                JSONObject body = new JSONObject();
                body.put("nama", nama);
                body.put("alamat", alamat);
                body.put("no_hp", hp);
                body.put("kode_referral", referral);
                byte[] bytes = body.toString().getBytes("UTF-8");
                try (java.io.OutputStream output = conn.getOutputStream()) {
                    output.write(bytes);
                }

                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (stream == null) throw new Exception("Server tidak memberikan respons.");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();
                JSONObject json = new JSONObject(result.toString());
                if (code < 200 || code >= 300) {
                    throw new Exception(json.optString("error", "Permohonan gagal dikirim."));
                }
                runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permohonan Terkirim")
                        .setMessage("Permohonan pendaftaran berhasil dikirim dan menunggu pemeriksaan admin. Silakan tunggu konfirmasi dari admin Nasyah Cell.")
                        .setPositiveButton("OK", (dialog, which) -> showLogin())
                        .setCancelable(false)
                        .show());
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Periksa koneksi server." : e.getMessage();
                runOnUiThread(() -> showMessage("Pendaftaran Gagal", msg));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private EditText authInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setTextColor(DARK);
        e.setHintTextColor(Color.rgb(90, 110, 118));
        e.setPadding(dp(15), dp(12), dp(15), dp(12));

        GradientDrawable border = new GradientDrawable();
        border.setColor(PANEL);
        border.setCornerRadius(dp(7));
        border.setStroke(dp(1), DARK);
        e.setBackground(border);
        return e;
    }

    private LinearLayout.LayoutParams authFieldLp() {
        LinearLayout.LayoutParams lp = wrap();
        lp.bottomMargin = dp(14);
        return lp;
    }

    private void prosesAuth(String endpoint, String kode, String password, String konfirmasi, String aktivasi) {
        if (kode.isEmpty() || password.isEmpty()) { showMessage("Login", "Kode reseller dan password wajib diisi."); return; }
        if (endpoint.equals("/api/daftar") && (aktivasi.isEmpty() || konfirmasi.isEmpty())) { showMessage("Pendaftaran", "Kode aktivasi dan konfirmasi password wajib diisi."); return; }
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST"); conn.setConnectTimeout(15000); conn.setReadTimeout(15000);
                conn.setDoOutput(true); conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("X-API-Key", API_KEY);
                JSONObject body = new JSONObject(); body.put("kode_reseller", kode); body.put("password", password);
                if (endpoint.equals("/api/daftar")) { body.put("konfirmasi_password", konfirmasi); body.put("kode_aktivasi", aktivasi); }
                byte[] bytes = body.toString().getBytes("UTF-8"); conn.getOutputStream().write(bytes);
                int code = conn.getResponseCode();
                java.io.InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder result = new StringBuilder(); String line; while ((line = reader.readLine()) != null) result.append(line); reader.close();
                JSONObject json = new JSONObject(result.toString());
                if (code < 200 || code >= 300) throw new Exception(json.optString("error", json.optString("pesan", "Permintaan gagal")));
                String token = json.optString("token", "");
                if (token.isEmpty()) throw new Exception("Server tidak mengirim token login.");
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_TOKEN, token).putString(PREF_KODE, kode).apply();
                tokenLogin = token;
                runOnUiThread(() -> { showMessage("Berhasil", endpoint.equals("/api/login") ? "Login berhasil." : "Pendaftaran berhasil. Anda sudah masuk."); showHome(); });
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Periksa koneksi server." : e.getMessage();
                runOnUiThread(() -> showMessage("Gagal", msg));
            } finally { if (conn != null) conn.disconnect(); }
        }).start();
    }

    private void cekSesiLaluMasuk() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + "/api/akun"); conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); conn.setConnectTimeout(12000); conn.setReadTimeout(12000);
                conn.setRequestProperty("X-API-Key", API_KEY); conn.setRequestProperty("Authorization", "Bearer " + tokenLogin);
                if (conn.getResponseCode() != 200) throw new Exception("Sesi login tidak berlaku.");
                runOnUiThread(() -> showHome());
            } catch (Exception e) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply(); tokenLogin = "";
                runOnUiThread(() -> showLogin());
            } finally { if (conn != null) conn.disconnect(); }
        }).start();
    }

    private void showHome() {
        selectedCategory = null;
        depositScreen = false;
        accountScreen = false;

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        // HEADER MERAH
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(15), dp(14), dp(15), dp(14));
        header.setBackgroundColor(RED);

        TextView menu = text("☰", 27, Color.WHITE, true);
        menu.setGravity(Gravity.CENTER);
        header.addView(menu, new LinearLayout.LayoutParams(dp(45), dp(45)));

        TextView appName = text("NASYAH CELL", 21, Color.WHITE, true);
        LinearLayout.LayoutParams nameLp =
                new LinearLayout.LayoutParams(0, -2, 1);
        nameLp.leftMargin = dp(12);
        header.addView(appName, nameLp);

        TextView refresh = text("⟳", 29, Color.WHITE, true);
        refresh.setGravity(Gravity.CENTER);
        header.addView(refresh, new LinearLayout.LayoutParams(dp(42), dp(45)));

        TextView more = text("⋮", 29, Color.WHITE, true);
        more.setGravity(Gravity.CENTER);
        header.addView(more, new LinearLayout.LayoutParams(dp(35), dp(45)));

        menu.setOnClickListener(v -> showMessage(
                "NASYAH CELL", "Selamat datang di layanan Nasyah Cell."
        ));

        refresh.setOnClickListener(v -> ambilSaldo());

        more.setOnClickListener(v -> showMoreMenu(v));

        root.addView(header);

        // TAB NAVIGASI
        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabScroll.setBackgroundColor(DARK);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);

        String[] tabNames = {
                "PRABAYAR", "PEMBAYARAN", "TOUR & TRAVEL", "MENU"
        };

        for (String tab : tabNames) {
            TextView item = text(tab, 14, Color.WHITE, true);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(18), dp(17), dp(18), dp(17));

            tabs.addView(item);

            item.setOnClickListener(v -> {
                if (tab.equals("PRABAYAR")) {
                    showHome();
                } else if (tab.equals("PEMBAYARAN")) {
                    showCategory("Tagihan PLN");
                } else {
                    showMessage(tab, "Menu ini akan dikembangkan.");
                }
            });
        }

        tabScroll.addView(tabs);
        root.addView(tabScroll);

        // KONTEN YANG BISA DIGULIR
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(16), dp(14), dp(20));

        scroll.addView(content);

        // BANNER PROMO
        LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(20), dp(18), dp(15), dp(18));
        banner.setBackground(round(RED, 18));

        LinearLayout bannerText = new LinearLayout(this);
        bannerText.setOrientation(LinearLayout.VERTICAL);

        TextView promoTitle = text("Produk Promo", 23, Color.WHITE, true);
        bannerText.addView(promoTitle);

        TextView promoSubtitle = text(
                "Buruan beli sekarang", 14, Color.WHITE, true
        );
        LinearLayout.LayoutParams promoSubLp = wrap();
        promoSubLp.topMargin = dp(5);
        bannerText.addView(promoSubtitle, promoSubLp);

        TextView promoButton = text(
                "  Klik Disini  ", 13, Color.WHITE, true
        );
        promoButton.setGravity(Gravity.CENTER);
        promoButton.setPadding(dp(10), dp(8), dp(10), dp(8));
        promoButton.setBackground(round(DARK, 20));

        LinearLayout.LayoutParams promoButtonLp = wrap();
        promoButtonLp.topMargin = dp(12);
        bannerText.addView(promoButton, promoButtonLp);

        banner.addView(
                bannerText,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        TextView promoIcon = text("🎉\nSUPER\nSALE!", 24, Color.WHITE, true);
        promoIcon.setGravity(Gravity.CENTER);
        banner.addView(promoIcon, new LinearLayout.LayoutParams(
                dp(125), dp(125)
        ));

        LinearLayout.LayoutParams bannerLp = wrap();
        bannerLp.height = dp(155);
        content.addView(banner, bannerLp);

        promoButton.setOnClickListener(v -> showMessage(
                "Produk Promo", "Daftar produk promo akan ditampilkan di sini."
        ));

        // KARTU SALDO
        LinearLayout saldoCard = new LinearLayout(this);
        saldoCard.setOrientation(LinearLayout.VERTICAL);
        saldoCard.setBackground(round(Color.WHITE, 12));
        saldoCard.setClipToOutline(true);

        LinearLayout saldoTop = new LinearLayout(this);
        saldoTop.setGravity(Gravity.CENTER_VERTICAL);
        saldoTop.setPadding(dp(15), dp(13), dp(15), dp(13));

        TextView walletIcon = text("💰", 20, Color.BLACK, false);
        saldoTop.addView(walletIcon);

        TextView saldoLabel = text("Saldo Anda", 18, Color.BLACK, false);
        LinearLayout.LayoutParams saldoLabelLp = wrap();
        saldoLabelLp.leftMargin = dp(10);
        saldoTop.addView(saldoLabel, saldoLabelLp);

        View spacer = new View(this);
        saldoTop.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        TextView eye = text("◉", 20, Color.BLACK, true);
        saldoTop.addView(eye);

        balanceText = text("Rp 0", 17, Color.BLACK, true);
        LinearLayout.LayoutParams balanceLp = wrap();
        balanceLp.leftMargin = dp(12);
        saldoTop.addView(balanceText, balanceLp);

        eye.setOnClickListener(v -> {
            saldoVisible = !saldoVisible;
            String nominal = formatRupiah(saldoOtomax);
            balanceText.setText(saldoVisible ? nominal : "******");
        });

        saldoCard.addView(saldoTop);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(210, 220, 225));
        saldoCard.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout saldoBottom = new LinearLayout(this);
        saldoBottom.setGravity(Gravity.CENTER);
        saldoBottom.setOrientation(LinearLayout.HORIZONTAL);

        saldoAmountText = text("💰  Rp 0", 17, Color.BLACK, false);
        saldoAmountText.setGravity(Gravity.CENTER);

        TextView deposit = text("➕ Isi Saldo", 17, Color.BLACK, false);
        deposit.setGravity(Gravity.CENTER);

        saldoBottom.addView(saldoAmountText,
                new LinearLayout.LayoutParams(0, dp(54), 1));

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(210, 220, 225));
        saldoBottom.addView(divider,
                new LinearLayout.LayoutParams(dp(1), dp(54)));

        saldoBottom.addView(deposit,
                new LinearLayout.LayoutParams(0, dp(54), 1));

        saldoCard.addView(saldoBottom);

        LinearLayout.LayoutParams saldoCardLp = wrap();
        saldoCardLp.topMargin = dp(20);
        content.addView(saldoCard, saldoCardLp);

        deposit.setOnClickListener(v -> showDeposit());
        
        // JUDUL PRODUK FAVORIT
        TextView favoriteTitle = text(
                "Produk Favorit", 19, Color.BLACK, true
        );
        LinearLayout.LayoutParams favoriteLp = wrap();
        favoriteLp.topMargin = dp(20);
        content.addView(favoriteTitle, favoriteLp);

        TextView favoriteSub = text(
                "Kami memudahkan pembelian produk untukmu",
                13, Color.GRAY, false
        );
        LinearLayout.LayoutParams favoriteSubLp = wrap();
        favoriteSubLp.topMargin = dp(3);
        favoriteSubLp.bottomMargin = dp(18);
        content.addView(favoriteSub, favoriteSubLp);

        // GRID PRODUK 4 KOLOM
        for (int i = 0; i < categories.length; i += 4) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);

            for (int j = i; j < i + 4 && j < categories.length; j++) {
                addProductItem(row, j);
            }

            LinearLayout.LayoutParams rowLp = wrap();
            rowLp.bottomMargin = dp(18);
            content.addView(row, rowLp);
        }

        TextView newTitle = text(
                "Produk Baru", 19, Color.BLACK, true
        );
        LinearLayout.LayoutParams newTitleLp = wrap();
        newTitleLp.topMargin = dp(5);
        content.addView(newTitle, newTitleLp);

        TextView newSub = text(
                "Layanan terbaru Nasyah Cell",
                13, Color.GRAY, false
        );
        content.addView(newSub);

        TextView newProduct = text(
                "🛒  Layanan Digital",
                16, BLUE, true
        );
        newProduct.setGravity(Gravity.CENTER);
        newProduct.setPadding(dp(12), dp(18), dp(12), dp(18));
        newProduct.setBackground(round(Color.WHITE, 12));

        LinearLayout.LayoutParams newProductLp = wrap();
        newProductLp.topMargin = dp(12);
        content.addView(newProduct, newProductLp);

        newProduct.setOnClickListener(v -> showCategory("Layanan Digital"));

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        // NAVIGASI BAWAH
        addBottomNavigation();

        setContentView(root);
        ambilSaldo(); // Ambil saldo otomatis saat halaman utama dibuka.
    }

    private String formatRupiah(double nilai) {
        return "Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID"))
                .format(nilai);
    }

    private void ambilSaldo() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + "/api/akun");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("X-API-Key", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + tokenLogin);

                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (stream == null) {
                    throw new Exception("Server tidak mengirim respons (HTTP " + code + ")");
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                if (code != 200) {
                    throw new Exception("HTTP " + code + ": " + response);
                }

                JSONObject json = new JSONObject(response.toString());
                double saldo;
                if (json.has("saldo")) {
                    saldo = json.getDouble("saldo");
                } else if (json.has("data") && json.optJSONObject("data") != null
                        && json.getJSONObject("data").has("saldo")) {
                    saldo = json.getJSONObject("data").getDouble("saldo");
                } else {
                    throw new Exception("Field saldo tidak ditemukan dalam respons server: " + response);
                }

                runOnUiThread(() -> {
                    saldoOtomax = saldo;
                    String nominal = formatRupiah(saldo);
                    if (balanceText != null) {
                        balanceText.setText(saldoVisible ? nominal : "******");
                    }
                    if (saldoAmountText != null) {
                        saldoAmountText.setText("💰  " + nominal);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showMessage("Gagal mengambil saldo",
                        e.getMessage() == null ? "Periksa koneksi server dan API." : e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void addProductItem(LinearLayout row, int index) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(2), dp(3), dp(2), dp(3));

        TextView icon = text(icons[index], 32, RED, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(Color.WHITE, 20));

        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                dp(76), dp(76)
        );
        item.addView(icon, iconLp);

        TextView label = text(
                categories[index], 12, Color.BLACK, false
        );
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);

        LinearLayout.LayoutParams labelLp = wrap();
        labelLp.topMargin = dp(9);
        item.addView(label, labelLp);

        row.addView(item, new LinearLayout.LayoutParams(0, -2, 1));

        item.setOnClickListener(v -> showCategory(categories[index]));
    }

    private void addBottomNavigation() {
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(Color.WHITE);
        bottom.setElevation(dp(8));

        String[] names = {
                "Kontak CS", "Riwayat", "QRIS", "Penjualan", "Akun"
        };

        String[] symbols = {
                "☎️", "📱", "▦", "📈", "👤"
        };

        for (int i = 0; i < names.length; i++) {
            final int index = i;

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(2), dp(9), dp(2), dp(8));

            TextView symbol = text(
                    symbols[i], i == 2 ? 27 : 21, RED, true
            );
            symbol.setGravity(Gravity.CENTER);

            if (i == 2) {
                symbol.setTextColor(Color.WHITE);
                symbol.setBackground(round(RED, 50));
                item.addView(symbol, new LinearLayout.LayoutParams(
                        dp(57), dp(57)
                ));
            } else {
                item.addView(symbol, new LinearLayout.LayoutParams(
                        -1, dp(35)
                ));
            }

            TextView label = text(
                    names[i], 11, Color.BLACK, false
            );
            label.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams labelLp = wrap();
            labelLp.topMargin = dp(3);
            item.addView(label, labelLp);

            bottom.addView(item,
                    new LinearLayout.LayoutParams(0, -2, 1));

            item.setOnClickListener(v -> {
                switch (index) {
                    case 0:
                        showMessage("Kontak CS",
                                "Kontak Customer Service akan dihubungkan di sini.");
                        break;
                    case 1:
                        showHistory();
                        break;
                    case 2:
                        showQRIS();
                        break;
                    case 3:
                        showMessage("Penjualan",
                                "Menu penjualan akan dikembangkan.");
                        break;
                    case 4:
                        showAccount();
                        break;
                }
            });
        }

        root.addView(bottom);
    }

    private void showCategory(String category) {
        selectedCategory = category;
        showProduct(category);
    }

    private void showProduct(String category) {
        selectedProviderCode = null;
        selectedProduct = null;
        daftarProvider.clear();
        daftarProduk.clear();
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(20), dp(18), dp(20));
        page.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("‹", 35, RED, true);
        top.addView(back, new LinearLayout.LayoutParams(dp(45), dp(50)));

        TextView title = text(category, 22, Color.BLACK, true);
        top.addView(title);

        page.addView(top);

        back.setOnClickListener(v -> showHome());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackground(round(Color.WHITE, 15));

        TextView numberLabel = text(
                "Nomor Tujuan", 15, Color.BLACK, true
        );
        card.addView(numberLabel);

        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setGravity(Gravity.CENTER_VERTICAL);

        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setHint("Masukkan nomor tujuan");
        numberInput.setInputType(3);
        numberInput.setTextSize(15);
        numberInput.setPadding(dp(10), 0, dp(10), 0);
        numberInput.setBackground(round(BG, 10));

        LinearLayout.LayoutParams numberLp =
                new LinearLayout.LayoutParams(0, dp(50), 1);
        numberLp.rightMargin = dp(8);
        contactRow.addView(numberInput, numberLp);

        TextView contactButton = button("Kontak");
        contactRow.addView(contactButton,
                new LinearLayout.LayoutParams(dp(85), dp(50)));

        contactButton.setOnClickListener(v -> pickContact());

        LinearLayout.LayoutParams contactLp = wrap();
        contactLp.topMargin = dp(10);
        card.addView(contactRow, contactLp);

        providerNameText = text("", 16, BLUE, true);
        LinearLayout.LayoutParams providerLp = wrap();
        providerLp.topMargin = dp(20);
        card.addView(providerNameText, providerLp);
        providerNameText.setVisibility(View.GONE);

        productList = new LinearLayout(this);
        productList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp = wrap();
        listLp.topMargin = dp(10);
        card.addView(productList, listLp);
        productList.setVisibility(View.GONE);

        numberInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                selectedProduct = null;
                selectedProviderCode = null;
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                final String nomor = s.toString().replaceAll("[^0-9]", "");
                searchRunnable = () -> {
                    boolean showResultsArea = nomor.length() >= 4;
                    int visibility = showResultsArea ? View.VISIBLE : View.GONE;
                    if (providerNameText != null) providerNameText.setVisibility(visibility);
                    if (productList != null) productList.setVisibility(visibility);
                    if (statusText != null) statusText.setVisibility(visibility);
                    if (showResultsArea) {
                        if (providerNameText != null) providerNameText.setText("Mencari provider...");
                        loadProducts(category, nomor);
                    } else {
                        daftarProduk.clear();
                        daftarProvider.clear();
                        selectedProviderCode = null;
                        selectedProduct = null;
                        if (providerNameText != null) providerNameText.setText("");
                        if (statusText != null) statusText.setText("");
                        if (productList != null) productList.removeAllViews();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Jika nomor sudah tersedia dari kontak, watcher akan memuat produk.
        if (numberInput.getText().length() >= 4) {
            loadProducts(category, numberInput.getText().toString());
        }

        page.addView(card);

        // Area daftar dapat digulir, tombol tetap menempel di bawah seperti AnggiCharger.
        ScrollView pageScroll = new ScrollView(this);
        pageScroll.setFillViewport(true);
        pageScroll.setVerticalScrollBarEnabled(true);
        pageScroll.setClipToPadding(false);
        pageScroll.addView(page, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(BG);
        screen.addView(pageScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView buy = button("LANJUTKAN");
        buy.setTextSize(16);
        buy.setBackground(round(Color.rgb(218, 43, 48), 8));
        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(18), dp(10), dp(18), dp(12));
        footer.setBackgroundColor(Color.WHITE);
        footer.setElevation(dp(8));
        footer.addView(buy, new LinearLayout.LayoutParams(-1, dp(54)));
        screen.addView(footer);

        setContentView(screen);
        buy.setOnClickListener(v -> processTransaction());
    }

    private void loadProducts(String category, String nomor) {
        if (providerNameText != null) providerNameText.setText("");
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String query = "/api/produk?kategori=" +
                        java.net.URLEncoder.encode(category, "UTF-8") +
                        "&nomor=" + java.net.URLEncoder.encode(nomor, "UTF-8");
                URL url = new URL(SERVER_URL + query);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("X-API-Key", API_KEY);
                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (stream == null) throw new Exception("Server tidak memberi respons HTTP " + code);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                if (code != 200) throw new Exception("HTTP " + code + ": " + response);
                JSONObject json = new JSONObject(response.toString());

                JSONArray providerJson = json.optJSONArray("provider");
                JSONArray productJson = json.optJSONArray("produk");
                ArrayList<JSONObject> providers = new ArrayList<>();
                ArrayList<JSONObject> products = new ArrayList<>();

                if (providerJson != null) {
                    for (int i = 0; i < providerJson.length(); i++) {
                        JSONObject item = providerJson.optJSONObject(i);
                        if (item != null) providers.add(item);
                    }
                }
                if (productJson != null) {
                    for (int i = 0; i < productJson.length(); i++) {
                        JSONObject item = productJson.optJSONObject(i);
                        if (item != null &&
                                item.optString("kategori", "").trim().equalsIgnoreCase(category)) {
                            products.add(item);
                        }
                    }
                }

                // Jika server lama belum mengirim array provider, susun dari produk.
                if (providers.isEmpty()) {
                    java.util.HashSet<String> seen = new java.util.HashSet<>();
                    for (JSONObject product : products) {
                        String providerCode = product.optString("kode_provider", "");
                        String key = providerCode.isEmpty()
                                ? product.optString("nama_provider", "") : providerCode;
                        if (seen.add(key)) {
                            JSONObject provider = new JSONObject();
                            provider.put("kode_provider", providerCode);
                            provider.put("nama_provider", product.optString("nama_provider", "Provider"));
                            providers.add(provider);
                        }
                    }
                }

                runOnUiThread(() -> {
                    daftarProvider.clear();
                    daftarProvider.addAll(providers);
                    daftarProduk.clear();
                    daftarProduk.addAll(products);
                    selectedProduct = null;
                    selectedProviderCode = null;
                    if (providerNameText != null) {
                        providerNameText.setText("");
                    }
                    renderProductCards();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    daftarProduk.clear();
                    daftarProvider.clear();
                    selectedProviderCode = null;
                    if (providerNameText != null) providerNameText.setText("");
                    renderProductCards();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void renderProductCards() {
        if (productList == null) return;
        productList.removeAllViews();

        // Tahap pertama: tampilkan daftar nama provider, belum menampilkan harga.
        if (selectedProviderCode == null) {
            if (daftarProvider.isEmpty()) {
                TextView empty = text("Provider akan muncul setelah nomor dikenali", 13, Color.GRAY, false);
                empty.setPadding(dp(12), dp(18), dp(12), dp(18));
                productList.addView(empty);
                return;
            }
            for (JSONObject provider : daftarProvider) {
                String namaProvider = provider.optString("nama_provider", "Provider");
                String kodeProvider = provider.optString("kode_provider", "");
                LinearLayout providerCard = new LinearLayout(this);
                providerCard.setOrientation(LinearLayout.VERTICAL);
                providerCard.setPadding(dp(14), dp(14), dp(14), dp(14));
                providerCard.setBackground(round(Color.WHITE, 12));

                TextView name = text(namaProvider, 17, BLUE, true);
                providerCard.addView(name);
                LinearLayout.LayoutParams lp = wrap();
                lp.topMargin = dp(8);
                productList.addView(providerCard, lp);
                providerCard.setOnClickListener(v -> {
                    selectedProviderCode = kodeProvider.isEmpty() ? namaProvider : kodeProvider;
                    if (providerNameText != null) {
                        providerNameText.setText("Provider: " + namaProvider);
                    }
                    selectedProduct = null;
                    renderProductCards();
                });
            }
            return;
        }

        // Tombol kembali ke daftar provider.
        TextView backProviders = text("‹ Kembali ke daftar provider", 14, BLUE, true);
        backProviders.setPadding(0, dp(4), 0, dp(8));
        productList.addView(backProviders);
        backProviders.setOnClickListener(v -> {
            selectedProviderCode = null;
            selectedProduct = null;
            if (providerNameText != null) providerNameText.setText("");
            renderProductCards();
        });

        int count = 0;
        for (JSONObject item : daftarProduk) {
            String kodeProvider = item.optString("kode_provider", "");
            String namaProvider = item.optString("nama_provider", "Provider");
            String providerKey = kodeProvider.isEmpty() ? namaProvider : kodeProvider;
            if (!providerKey.equalsIgnoreCase(selectedProviderCode)) continue;
            count++;

            String nama = item.optString("nama_produk", "Produk");
            String kode = item.optString("kode_produk", "");
            String nominal = item.optString("nominal", "");
            String status = item.optString("status_produk", "OPEN").trim().toUpperCase(Locale.ROOT);
            boolean isOpen = status.equals("OPEN");
            double harga = item.optDouble("harga_jual", 0);
            boolean dipilih = item == selectedProduct;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(10), dp(12), dp(10));
            GradientDrawable border = round(dipilih ? Color.rgb(255, 246, 246) : Color.WHITE, 9);
            border.setStroke(dp(dipilih ? 2 : 1), dipilih ? RED : Color.rgb(160, 181, 184));
            card.setBackground(border);

            TextView ribbon = text(namaProvider.toUpperCase(Locale.ROOT), 11, Color.WHITE, true);
            ribbon.setGravity(Gravity.CENTER_VERTICAL);
            ribbon.setPadding(dp(9), dp(4), dp(9), dp(4));
            ribbon.setBackground(round(RED, 7));
            LinearLayout.LayoutParams ribbonLp = new LinearLayout.LayoutParams(-2, -2);
            ribbonLp.bottomMargin = dp(8);
            card.addView(ribbon, ribbonLp);

            LinearLayout main = new LinearLayout(this);
            main.setOrientation(LinearLayout.HORIZONTAL);
            main.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            TextView codeText = text(kode.isEmpty() ? nama : kode, 18, DARK, true);
            info.addView(codeText);
            String deskripsi = nama;
            if (!nominal.isEmpty() && !nama.toLowerCase(Locale.ROOT).contains(nominal.toLowerCase(Locale.ROOT))) {
                deskripsi += " " + nominal;
            }
            TextView descText = text(deskripsi, 12, DARK, false);
            LinearLayout.LayoutParams descLp = wrap();
            descLp.topMargin = dp(4);
            info.addView(descText, descLp);

            // Status ditempatkan di tengah, dan nilainya mengikuti status_produk dari server.
            TextView statusTextCard = text(isOpen ? "Open" : "Gangguan", 12,
                    isOpen ? Color.rgb(46, 125, 50) : RED, false);
            statusTextCard.setGravity(Gravity.CENTER);

            TextView price = text(formatRupiah(harga).replace("Rp ", ""), 19, DARK, true);
            price.setGravity(Gravity.RIGHT);

            main.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(dp(76), -2);
            main.addView(statusTextCard, statusLp);
            LinearLayout.LayoutParams priceLp = new LinearLayout.LayoutParams(-2, -2);
            priceLp.leftMargin = dp(8);
            main.addView(price, priceLp);
            card.addView(main);

            LinearLayout.LayoutParams cardLp = wrap();
            cardLp.topMargin = dp(8);
            productList.addView(card, cardLp);
            card.setOnClickListener(v -> {
                if (!isOpen) {
                    showMessage("Produk Gangguan", "Produk ini sedang gangguan dan belum bisa dipilih.");
                    return;
                }
                selectedProduct = item;
                renderProductCards();
                if (statusText != null) statusText.setText("Dipilih: " + nama + " | " + formatRupiah(harga));
            });
        }

        if (count == 0) {
            TextView empty = text("Belum ada produk untuk provider ini.", 13, Color.GRAY, false);
            empty.setPadding(dp(12), dp(18), dp(12), dp(18));
            productList.addView(empty);
        }
    }

    private void processTransaction() {
        String number = numberInput.getText().toString().trim();
        if (number.isEmpty() || number.length() < 4) {
            showMessage("Perhatian", "Masukkan nomor tujuan yang benar.");
            return;
        }
        if (selectedProduct == null) {
            showMessage("Perhatian", "Pilih salah satu produk terlebih dahulu.");
            return;
        }
        String nama = selectedProduct.optString("nama_produk", "Produk");
        String kode = selectedProduct.optString("kode_produk", "");
        String harga = formatRupiah(selectedProduct.optDouble("harga_jual", 0));
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Produk")
                .setMessage("Nomor: " + number + "\nProduk: " + nama +
                        "\nHarga: " + harga + "\nKode: " + kode +
                        "\n\nIni hanya pratinjau. Transaksi belum dikirim ke OtomaX.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void pickContact() {
        try {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            );
            startActivityForResult(intent, REQUEST_CONTACT);
        } catch (Exception e) {
            showMessage("Kontak", "Tidak dapat membuka kontak.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACT
                && resultCode == RESULT_OK
                && data != null) {

            Cursor cursor = null;

            try {
                cursor = getContentResolver().query(
                        data.getData(),
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        },
                        null, null, null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    String phone = cursor.getString(0);

                    if (numberInput != null) {
                        numberInput.setText(phone);
                    }
                }
            } catch (Exception e) {
                showMessage("Kontak", "Gagal mengambil nomor.");
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    // HALAMAN ISI SALDO - tata letak mengikuti contoh AnggiCharger.
    // Tombol dan form sudah dibuat; pembuatan tiket perlu endpoint deposit OtomaX.
    private void showDeposit() {
        depositScreen = true;

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.WHITE);

        // Header merah dengan tombol kembali dan refresh.
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(15), dp(12), dp(15), dp(12));
        header.setBackgroundColor(RED);

        TextView home = text("⌂", 29, Color.WHITE, true);
        home.setGravity(Gravity.CENTER);
        header.addView(home, new LinearLayout.LayoutParams(dp(48), dp(48)));
        home.setOnClickListener(v -> showHome());

        TextView headerTitle = text("NASYAH CELL", 19, Color.WHITE, true);
        LinearLayout.LayoutParams headerTitleLp =
                new LinearLayout.LayoutParams(0, -2, 1);
        headerTitleLp.leftMargin = dp(12);
        header.addView(headerTitle, headerTitleLp);

        TextView refresh = text("⟳", 29, Color.WHITE, true);
        refresh.setGravity(Gravity.CENTER);
        header.addView(refresh, new LinearLayout.LayoutParams(dp(45), dp(48)));
        refresh.setOnClickListener(v -> showDeposit());
        screen.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(18), dp(12), dp(25));
        page.setBackgroundColor(Color.WHITE);

        TextView heading = text("Tambah Saldo", 22, Color.WHITE, true);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(dp(14), dp(18), dp(14), dp(18));
        heading.setBackgroundColor(RED);
        page.addView(heading, wrap());

        TextView instructions = text(
                "\"TIDAK MENERIMA TRANSFER MELALUI MESIN EDC, NEKAT SALDO TIDAK AKAN BISA DIGUNAKAN.\\n\\n"
                + "Mohon sebelum transfer cek dulu nama dan rekening bank dan nominal. "
                + "Tambah saldo otomatis masuk jika sesuai nominal.\\n\\n"
                + "OPEN DEPOSIT jam 06.00 - 21.00 WIB.\"",
                16, DARK, false
        );
        instructions.setPadding(dp(12), dp(28), dp(12), dp(24));
        page.addView(instructions, wrap());

        LinearLayout formArea = new LinearLayout(this);
        formArea.setOrientation(LinearLayout.VERTICAL);
        formArea.setPadding(dp(5), dp(5), dp(5), dp(12));
        formArea.setVisibility(View.GONE);

        TextView formTitle = text("Silakan Isi", 19, DARK, true);
        LinearLayout.LayoutParams formTitleLp = wrap();
        formTitleLp.topMargin = dp(10);
        formTitleLp.bottomMargin = dp(20);
        formArea.addView(formTitle, formTitleLp);

        TextView nominalLabel = text("Nominal", 16, DARK, false);
        formArea.addView(nominalLabel);

        EditText nominalInput = new EditText(this);
        nominalInput.setSingleLine(true);
        nominalInput.setTextSize(17);
        nominalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        nominalInput.setPadding(dp(13), dp(10), dp(13), dp(10));
        nominalInput.setBackground(round(Color.WHITE, 8));
        GradientDrawable inputBorder = round(Color.WHITE, 8);
        inputBorder.setStroke(dp(1), DARK);
        nominalInput.setBackground(inputBorder);
        LinearLayout.LayoutParams nominalLp = wrap();
        nominalLp.topMargin = dp(10);
        nominalLp.bottomMargin = dp(18);
        formArea.addView(nominalInput, nominalLp);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView send = text("KIRIM", 17, Color.WHITE, true);
        send.setGravity(Gravity.CENTER);
        send.setBackground(round(DARK, 8));
        actionRow.addView(send, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView cancel = text("CANCEL", 17, Color.WHITE, true);
        cancel.setGravity(Gravity.CENTER);
        cancel.setBackground(round(RED, 8));
        LinearLayout.LayoutParams cancelLp =
                new LinearLayout.LayoutParams(0, dp(58), 1);
        cancelLp.leftMargin = dp(8);
        actionRow.addView(cancel, cancelLp);
        formArea.addView(actionRow);

        page.addView(formArea, wrap());

        TextView ticketButton = text("TIKET TAMBAH SALDO", 18, Color.WHITE, true);
        ticketButton.setGravity(Gravity.CENTER);
        ticketButton.setPadding(dp(10), dp(17), dp(10), dp(17));
        ticketButton.setBackground(round(DARK, 8));
        LinearLayout.LayoutParams ticketButtonLp = wrap();
        ticketButtonLp.leftMargin = dp(4);
        ticketButtonLp.rightMargin = dp(4);
        ticketButtonLp.topMargin = dp(10);
        ticketButtonLp.bottomMargin = dp(42);
        page.addView(ticketButton, ticketButtonLp);

        TextView ticketCount = text("Menampilkan 0 dari 0 data", 15, DARK, false);
        LinearLayout.LayoutParams countLp = wrap();
        countLp.leftMargin = dp(10);
        countLp.bottomMargin = dp(18);
        page.addView(ticketCount, countLp);

        TextView emptyTicket = text("Belum ada tiket deposit.", 15, DARK, false);
        emptyTicket.setGravity(Gravity.CENTER);
        emptyTicket.setPadding(dp(12), dp(22), dp(12), dp(22));
        emptyTicket.setBackgroundColor(Color.rgb(240, 240, 240));
        page.addView(emptyTicket, wrap());

        ticketButton.setOnClickListener(v -> {
            formArea.setVisibility(View.VISIBLE);
            ticketButton.setVisibility(View.GONE);
            nominalInput.requestFocus();
        });

        cancel.setOnClickListener(v -> {
            nominalInput.setText("");
            formArea.setVisibility(View.GONE);
            ticketButton.setVisibility(View.VISIBLE);
        });

        send.setOnClickListener(v -> {
            String nominal = nominalInput.getText().toString().trim();
            if (nominal.isEmpty() || Long.parseLong(nominal) < 10000) {
                showMessage("Nominal tidak valid",
                        "Masukkan nominal minimal Rp10.000.");
                return;
            }
            showMessage("Pembuatan tiket belum terhubung",
                    "Nominal Rp" + nominal + " sudah diisi. "
                    + "Tiket belum dibuat dan saldo belum bertambah karena "
                    + "endpoint pembuatan tiket deposit OtomaX belum tersedia.");
        });

        scroll.addView(page);
        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(screen);
    }

    private interface DepositClick { void onClick(View view); }

    private void addDepositCard(LinearLayout parent, String icon, String titleText, String detail, View.OnClickListener click) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(15), dp(16), dp(15), dp(16));
        card.setBackground(round(Color.WHITE, 14));
        TextView ico = text(icon, 27, RED, true);
        card.addView(ico, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout labels = new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL);
        TextView name = text(titleText, 16, Color.BLACK, true);
        TextView desc = text(detail, 12, Color.DKGRAY, false);
        labels.addView(name); LinearLayout.LayoutParams dlp = wrap(); dlp.topMargin = dp(4); labels.addView(desc, dlp);
        card.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(text("›", 28, DARK, true));
        LinearLayout.LayoutParams lp = wrap(); lp.bottomMargin = dp(11); parent.addView(card, lp);
        card.setOnClickListener(click);
    }

    private EditText depositInput(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true); input.setTextSize(16); input.setHint(hint);
        input.setPadding(dp(13), dp(10), dp(13), dp(10));
        input.setBackground(round(Color.rgb(246,247,249), 10));
        return input;
    }

    private LinearLayout depositPage(String heading) {
        depositScreen = true;
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(18), dp(18), dp(24)); page.setBackgroundColor(BG);
        TextView title = text(heading, 21, RED, true); title.setPadding(0, 0, 0, dp(15)); page.addView(title);
        return page;
    }

    private void showTicketDeposit() {
        LinearLayout page = depositPage("Tiket Deposit OtomaX");
        page.addView(text("Nominal Deposit", 14, Color.BLACK, true));
        EditText amount = depositInput("Contoh: 50000"); amount.setInputType(2);
        LinearLayout.LayoutParams alp = wrap(); alp.topMargin = dp(7); alp.bottomMargin = dp(15); page.addView(amount, alp);
        page.addView(text("Atau masukkan kode tiket yang sudah dibuat di OtomaX", 14, Color.BLACK, true));
        EditText ticket = depositInput("Nomor / kode tiket deposit");
        LinearLayout.LayoutParams tlp = wrap(); tlp.topMargin = dp(7); tlp.bottomMargin = dp(15); page.addView(ticket, tlp);
        TextView note = text("Kode tiket manual tidak langsung menambah saldo. Server harus memeriksa tiket ke OtomaX terlebih dahulu.", 13, Color.DKGRAY, false);
        note.setPadding(dp(12), dp(12), dp(12), dp(12)); note.setBackground(round(Color.rgb(225,239,255), 10)); page.addView(note);
        TextView submit = button("Periksa / Buat Tiket"); LinearLayout.LayoutParams slp = wrap(); slp.topMargin = dp(20); page.addView(submit, slp);
        submit.setOnClickListener(v -> {
            String code = ticket.getText().toString().trim();
            String nominal = amount.getText().toString().trim();
            if (code.isEmpty() && nominal.isEmpty()) { showMessage("Deposit", "Masukkan nominal atau kode tiket terlebih dahulu."); return; }
            showMessage("Belum terhubung", "Input sudah diterima, tetapi pemeriksaan tiket dan pembuatan tiket OtomaX memerlukan endpoint deposit pada Flask. Belum ada saldo yang ditambahkan.");
        });
        ScrollView scroll = new ScrollView(this); scroll.addView(page); setContentView(scroll);
    }

    private void showBankDeposit() {
        LinearLayout page = depositPage("Transfer Bank / SeaBank");
        page.addView(text("Nominal Deposit", 14, Color.BLACK, true));
        EditText amount = depositInput("Contoh: 100000"); amount.setInputType(2);
        LinearLayout.LayoutParams lp = wrap(); lp.topMargin = dp(7); page.addView(amount, lp);
        TextView info = text("Rekening tujuan belum diatur. Hubungkan data rekening resmi Nasyah Cell di server sebelum menerima transfer.", 14, Color.DKGRAY, false);
        info.setPadding(dp(14), dp(14), dp(14), dp(14)); info.setBackground(round(Color.WHITE, 12));
        LinearLayout.LayoutParams ilp = wrap(); ilp.topMargin = dp(15); page.addView(info, ilp);
        TextView btn = button("Lanjutkan Transfer"); LinearLayout.LayoutParams blp = wrap(); blp.topMargin = dp(18); page.addView(btn, blp);
        btn.setOnClickListener(v -> showMessage("Transfer belum aktif", "Nomor rekening dan verifikasi pembayaran belum dikonfigurasi. Jangan transfer ke rekening yang belum ditampilkan secara resmi."));
        ScrollView scroll = new ScrollView(this); scroll.addView(page); setContentView(scroll);
    }

    private void showQrisDeposit() {
        LinearLayout page = depositPage("Pembayaran QRIS");
        page.addView(text("Nominal Deposit", 14, Color.BLACK, true));
        EditText amount = depositInput("Contoh: 50000"); amount.setInputType(2);
        LinearLayout.LayoutParams lp = wrap(); lp.topMargin = dp(7); page.addView(amount, lp);
        TextView info = text("QRIS merchant Nasyah Cell belum dikonfigurasi. QR pembayaran akan ditampilkan setelah server terhubung dengan penyedia QRIS.", 14, Color.DKGRAY, false);
        info.setGravity(Gravity.CENTER); info.setPadding(dp(14), dp(25), dp(14), dp(25)); info.setBackground(round(Color.WHITE, 12));
        LinearLayout.LayoutParams ilp = wrap(); ilp.topMargin = dp(15); page.addView(info, ilp);
        TextView btn = button("Buat Pembayaran QRIS"); LinearLayout.LayoutParams blp = wrap(); blp.topMargin = dp(18); page.addView(btn, blp);
        btn.setOnClickListener(v -> showMessage("QRIS belum aktif", "Belum ada QRIS dinamis yang dibuat. Saldo tidak akan berubah sebelum pembayaran diverifikasi."));
        ScrollView scroll = new ScrollView(this); scroll.addView(page); setContentView(scroll);
    }

    private void showQRIS() {
        new AlertDialog.Builder(this)
                .setTitle("PEMBAYARAN QRIS")
                .setMessage(
                        "Fitur QRIS belum terhubung dengan penyedia pembayaran."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHistory() {
        showMessage("Riwayat Transaksi",
                "Belum ada transaksi.");
    }

    private void showAccount() {
        accountScreen = true;
        depositScreen = false;
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(15), dp(12), dp(15), dp(12));
        header.setBackgroundColor(RED);

        TextView back = text("‹", 36, Color.WHITE, true);
        back.setGravity(Gravity.CENTER);
        header.addView(back, new LinearLayout.LayoutParams(dp(42), dp(44)));
        back.setOnClickListener(v -> showHome());

        TextView title = text("AKUN SAYA", 19, Color.WHITE, true);
        LinearLayout.LayoutParams titleLp =
                new LinearLayout.LayoutParams(0, -2, 1);
        titleLp.leftMargin = dp(10);
        header.addView(title, titleLp);

        TextView refresh = text("⟳", 28, Color.WHITE, true);
        refresh.setGravity(Gravity.CENTER);
        header.addView(refresh, new LinearLayout.LayoutParams(dp(42), dp(44)));
        refresh.setOnClickListener(v -> ambilSaldo());
        screen.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(16), dp(14), dp(24));

        LinearLayout profile = new LinearLayout(this);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(dp(16), dp(18), dp(16), dp(18));
        profile.setBackground(round(Color.WHITE, 12));

        TextView avatar = text("👤", 34, BLUE, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(Color.rgb(225, 239, 250), 50));
        profile.addView(avatar, new LinearLayout.LayoutParams(dp(62), dp(62)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(13), 0, 0, 0);
        identity.addView(text("Reseller OtomaX", 17, DARK, true));
        identity.addView(text("Profil akan dimuat dari akun OtomaX", 12, Color.GRAY, false));
        profile.addView(identity, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(profile, wrap());

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams summaryLp = wrap();
        summaryLp.topMargin = dp(12);
        page.addView(summary, summaryLp);

        LinearLayout saldoCard = new LinearLayout(this);
        saldoCard.setOrientation(LinearLayout.VERTICAL);
        saldoCard.setPadding(dp(14), dp(14), dp(10), dp(14));
        saldoCard.setBackground(round(Color.WHITE, 10));
        saldoCard.addView(text("SALDO RESELLER", 11, Color.GRAY, true));
        saldoCard.addView(text("Lihat di Beranda", 16, DARK, true));
        summary.addView(saldoCard, new LinearLayout.LayoutParams(0, dp(82), 1));

        LinearLayout poinCard = new LinearLayout(this);
        poinCard.setOrientation(LinearLayout.VERTICAL);
        poinCard.setPadding(dp(14), dp(14), dp(10), dp(14));
        poinCard.setBackground(round(Color.WHITE, 10));
        LinearLayout.LayoutParams poinLp = new LinearLayout.LayoutParams(0, dp(82), 1);
        poinLp.leftMargin = dp(10);
        summary.addView(poinCard, poinLp);
        poinCard.addView(text("POIN SAYA", 11, Color.GRAY, true));
        poinCard.addView(text("Belum tersedia", 16, DARK, true));

        TextView info = text(
                "Poin dan riwayat poin akan mengambil data dari OtomaX. "
                + "Sumber data poin belum teridentifikasi, jadi jumlahnya "
                + "belum ditampilkan agar tidak menampilkan angka yang keliru.",
                12, Color.DKGRAY, false);
        info.setPadding(dp(4), dp(12), dp(4), dp(14));
        page.addView(info, wrap());

        TextView section = text("MENU AKUN", 12, Color.GRAY, true);
        LinearLayout.LayoutParams sectionLp = wrap();
        sectionLp.bottomMargin = dp(8);
        page.addView(section, sectionLp);

        addAccountRow(page, "👤", "Profil Reseller",
                "Informasi akun yang terdaftar di OtomaX",
                () -> showMessage("Profil Reseller",
                        "Profil akan ditampilkan setelah endpoint data akun tersedia."));
        addAccountRow(page, "🪙", "Poin Saya",
                "Lihat jumlah poin yang dimiliki",
                () -> showMessage("Poin Saya",
                        "Jumlah poin belum dapat dibaca karena sumber data poin OtomaX belum diketahui."));
        addAccountRow(page, "📜", "Riwayat Poin",
                "Catatan poin masuk dan keluar",
                () -> showMessage("Riwayat Poin",
                        "Riwayat poin akan dihubungkan setelah tabel atau modul poin OtomaX teridentifikasi."));
        addAccountRow(page, "🔒", "Ubah Password",
                "Pengaturan password akun reseller",
                () -> showMessage("Ubah Password",
                        "Fitur ini perlu memakai mekanisme resmi OtomaX, bukan mengubah password langsung dari aplikasi."));
        addAccountRow(page, "🧾", "Riwayat Transaksi",
                "Lihat transaksi pembelian",
                () -> showHistory());
        addAccountRow(page, "💳", "Riwayat Deposit",
                "Lihat catatan deposit dan tiket",
                () -> showMessage("Riwayat Deposit",
                        "Riwayat deposit belum terhubung ke endpoint tiket OtomaX."));
        addAccountRow(page, "☎️", "Hubungi Admin",
                "Bantuan Nasyah Cell",
                () -> showMessage("Hubungi Admin",
                        "Kontak admin belum dikonfigurasi."));

        TextView logout = text("KELUAR AKUN", 14, RED, true);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(dp(12), dp(16), dp(12), dp(16));
        logout.setBackground(round(Color.WHITE, 10));
        LinearLayout.LayoutParams logoutLp = wrap();
        logoutLp.topMargin = dp(10);
        page.addView(logout, logoutLp);
        logout.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Keluar akun?")
                .setMessage("Apakah ingin keluar dari Nasyah Cell?")
                .setNegativeButton("BATAL", null)
                .setPositiveButton("KELUAR", (dialog, which) -> {
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
                    tokenLogin = "";
                    showLogin();
                })
                .show());

        scroll.addView(page);
        screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(screen);
    }

    private void addAccountRow(LinearLayout parent, String iconText,
                               String labelText, String subtitle,
                               Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(11), dp(12), dp(11));
        row.setBackground(round(Color.WHITE, 9));

        TextView icon = text(iconText, 22, BLUE, true);
        icon.setGravity(Gravity.CENTER);
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(10), 0, 0, 0);
        labels.addView(text(labelText, 14, DARK, true));
        labels.addView(text(subtitle, 11, Color.GRAY, false));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text("›", 25, Color.GRAY, false));

        LinearLayout.LayoutParams rowLp = wrap();
        rowLp.bottomMargin = dp(7);
        parent.addView(row, rowLp);
        row.setOnClickListener(v -> action.run());
    }

    private void showMoreMenu(View anchor) {
        String[] options = {
                "Beranda", "Riwayat", "Isi Saldo", "Akun"
        };

        new AlertDialog.Builder(this)
                .setTitle("Menu NASYAH CELL")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showHome();
                            break;
                        case 1:
                            showHistory();
                            break;
                        case 2:
                            showDeposit();
                            break;
                        case 3:
                            showAccount();
                            break;
                    }
                })
                .show();
    }

    private TextView button(String value) {
        TextView b = text(value, 15, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), dp(12), dp(10), dp(12));
        b.setBackground(round(RED, 12));
        return b;
    }

    private TextView text(
            String value, int size, int color, boolean bold
    ) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(null, Typeface.BOLD);
        }

        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }

    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (accountScreen) {
            showHome();
        } else if (depositScreen) {
            showHome();
        } else if (selectedCategory != null) {
            showHome();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Keluar Aplikasi")
                    .setMessage("Yakin ingin keluar dari NASYAH CELL?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton("Ya",
                            (dialog, which) -> finish())
                    .show();
        }
    }
}
