
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

    private LinearLayout root;
    private LinearLayout content;
    private TextView balanceText;
    private TextView saldoAmountText;
    private double saldoOtomax = 0;

    // Sesuaikan URL ini jika alamat Cloudflare Tunnel sudah berubah.
    private static final String SERVER_URL =
            "https://wines-liberal-interesting-sur.trycloudflare.com";

    // Isi dengan API key yang sama persis seperti di config.json server.
    private static final String API_KEY = "NasyahCell-2026-KunciRahasia-73921";

    private EditText numberInput;
    private LinearLayout productList;
    private JSONObject selectedProduct;
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;
    private TextView statusText;
    private TextView providerNameText;
    private final ArrayList<JSONObject> daftarProduk = new ArrayList<>();

    private boolean saldoVisible = false;
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

        showHome();
    }

    private void showHome() {
        selectedCategory = null;

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
                URL url = new URL(SERVER_URL + "/api/saldo");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("X-API-Key", API_KEY);

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

        providerNameText = text("Provider: menunggu nomor tujuan", 16, BLUE, true);
        LinearLayout.LayoutParams providerLp = wrap();
        providerLp.topMargin = dp(20);
        card.addView(providerNameText, providerLp);

        TextView productLabel = text("Daftar Produk", 14, Color.BLACK, true);
        LinearLayout.LayoutParams productLabelLp = wrap();
        productLabelLp.topMargin = dp(12);
        card.addView(productLabel, productLabelLp);

        productList = new LinearLayout(this);
        productList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp = wrap();
        listLp.topMargin = dp(10);
        card.addView(productList, listLp);

        statusText = text("Ketik nomor HP minimal 4 angka. Produk akan mengikuti prefix dan Catatan Provider OtomaX.",
                13, Color.GRAY, false);
        LinearLayout.LayoutParams statusLp = wrap();
        statusLp.topMargin = dp(12);
        card.addView(statusText, statusLp);

        numberInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                selectedProduct = null;
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                final String nomor = s.toString().replaceAll("[^0-9]", "");
                searchRunnable = () -> {
                    if (nomor.length() >= 4) loadProducts(category, nomor);
                    else {
                        daftarProduk.clear();
                        if (providerNameText != null) providerNameText.setText("Provider: menunggu nomor tujuan");
                        renderProductCards();
                        if (statusText != null) statusText.setText("Masukkan minimal 4 angka nomor HP.");
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

        TextView note = text(
                "Pilih produk yang diinginkan, lalu tekan LANJUTKAN.",
                12, Color.GRAY, false
        );
        LinearLayout.LayoutParams noteLp = wrap();
        noteLp.topMargin = dp(12);
        page.addView(note, noteLp);

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
        if (statusText != null) statusText.setText("Mencari provider berdasarkan prefix nomor...");
        if (providerNameText != null) providerNameText.setText("Provider: sedang dideteksi...");
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
                JSONArray items = json.optJSONArray("produk");
                ArrayList<JSONObject> hasil = new ArrayList<>();
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.optJSONObject(i);
                        if (item == null) continue;
                        if (item.optString("kategori", "").trim().equalsIgnoreCase(category)
                                && item.optString("status_produk", "OPEN").equalsIgnoreCase("OPEN")) {
                            hasil.add(item);
                        }
                    }
                }
                String namaProvider = hasil.isEmpty() ? "Provider tidak dikenali" :
                        hasil.get(0).optString("nama_provider", "Provider tidak dikenali");
                runOnUiThread(() -> {
                    daftarProduk.clear();
                    daftarProduk.addAll(hasil);
                    selectedProduct = null;
                    if (providerNameText != null) {
                        providerNameText.setText("Provider: " + namaProvider);
                    }
                    renderProductCards();
                    if (statusText != null) {
                        statusText.setText(hasil.isEmpty()
                                ? "Provider tidak ditemukan atau belum memiliki produk pada kategori ini. Periksa Filter Prefix Tujuan dan Catatan di OtomaX."
                                : "Ditemukan " + hasil.size() + " produk untuk nomor " + nomor + ".");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    daftarProduk.clear();
                    if (providerNameText != null) providerNameText.setText("Provider: gagal dideteksi");
                    renderProductCards();
                    if (statusText != null) statusText.setText("Gagal memuat produk: " + e.getMessage());
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void renderProductCards() {
        if (productList == null) return;
        productList.removeAllViews();
        if (daftarProduk.isEmpty()) {
            TextView empty = text("Produk akan muncul setelah nomor dikenali", 13, Color.GRAY, false);
            empty.setPadding(dp(12), dp(18), dp(12), dp(18));
            productList.addView(empty);
            return;
        }

        for (JSONObject item : daftarProduk) {
            String nama = item.optString("nama_produk", "Produk");
            String kode = item.optString("kode_produk", "");
            String nominal = item.optString("nominal", "");
            String provider = item.optString("nama_provider", "NASYAH CELL");
            String status = item.optString("status_produk", "OPEN").toUpperCase(Locale.ROOT);
            double harga = item.optDouble("harga_jual", 0);
            boolean dipilih = item == selectedProduct;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(10), dp(12), dp(10));
            GradientDrawable border = round(dipilih ? Color.rgb(255, 246, 246) : Color.WHITE, 9);
            border.setStroke(dp(dipilih ? 2 : 1), dipilih ? RED : Color.rgb(160, 181, 184));
            card.setBackground(border);

            TextView ribbon = text(provider.toUpperCase(Locale.ROOT), 11, Color.WHITE, true);
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

            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            right.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            TextView statusTextCard = text(status.equals("OPEN") ? "Open" : status, 12,
                    status.equals("OPEN") ? Color.rgb(46, 125, 50) : RED, false);
            statusTextCard.setGravity(Gravity.RIGHT);
            right.addView(statusTextCard);
            TextView price = text(formatRupiah(harga).replace("Rp ", ""), 19, DARK, true);
            price.setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams priceLp = wrap();
            priceLp.topMargin = dp(5);
            right.addView(price, priceLp);

            main.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(-2, -2);
            rightLp.leftMargin = dp(8);
            main.addView(right, rightLp);
            card.addView(main);

            LinearLayout.LayoutParams cardLp = wrap();
            cardLp.topMargin = dp(8);
            productList.addView(card, cardLp);
            card.setOnClickListener(v -> {
                selectedProduct = item;
                renderProductCards();
                if (statusText != null) statusText.setText("Dipilih: " + nama + " | " + formatRupiah(harga));
            });
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

    private void showDeposit() {
        new AlertDialog.Builder(this)
                .setTitle("ISI SALDO NASYAH CELL")
                .setMessage(
                        "Menu deposit siap dikembangkan.\n\n"
                                + "Pembayaran bank dan QRIS akan "
                                + "dihubungkan setelah sistem deposit tersedia."
                )
                .setPositiveButton("OK", null)
                .show();
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
        showMessage("Akun NASYAH CELL",
                "Menu profil dan pengaturan akun akan dikembangkan.");
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
        if (selectedCategory != null) {
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
