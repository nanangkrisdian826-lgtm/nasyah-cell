
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
    private Spinner productSpinner;
    private TextView statusText;
    private LinearLayout productContainer;
    private JSONObject selectedProduct;
    private final ArrayList<JSONObject> daftarProduk = new ArrayList<>();
    private final ArrayList<JSONObject> semuaProduk = new ArrayList<>();
    private String kategoriAktif = "";
    private String providerAktif = "";
    private String nomorPrefixAktif = "";
    private boolean sedangMemuatProduk = false;
    private ArrayAdapter<String> productAdapter;

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
        selectedProduct = null;
        providerAktif = "";
        nomorPrefixAktif = "";
        kategoriAktif = category;
        semuaProduk.clear();
        daftarProduk.clear();
        sedangMemuatProduk = false;
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(16), dp(8), dp(16), dp(8));
        top.setBackgroundColor(Color.WHITE);

        TextView back = text("‹", 35, RED, true);
        top.addView(back, new LinearLayout.LayoutParams(dp(45), dp(50)));
        TextView title = text(category, 21, Color.BLACK, true);
        top.addView(title);
        page.addView(top);
        back.setOnClickListener(v -> showHome());

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(16), dp(16), dp(22));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackground(round(Color.WHITE, 15));

        card.addView(text("Nomor Tujuan", 15, Color.BLACK, true));
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setGravity(Gravity.CENTER_VERTICAL);
        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setHint("Masukkan nomor tujuan");
        numberInput.setInputType(3);
        numberInput.setTextSize(15);
        numberInput.setPadding(dp(10), 0, dp(10), 0);
        numberInput.setBackground(round(BG, 10));
        LinearLayout.LayoutParams numberLp = new LinearLayout.LayoutParams(0, dp(50), 1);
        numberLp.rightMargin = dp(8);
        contactRow.addView(numberInput, numberLp);
        TextView contactButton = button("Kontak");
        contactRow.addView(contactButton, new LinearLayout.LayoutParams(dp(85), dp(50)));
        contactButton.setOnClickListener(v -> pickContact());
        LinearLayout.LayoutParams contactLp = wrap();
        contactLp.topMargin = dp(10);
        card.addView(contactRow, contactLp);
        body.addView(card);

        TextView productLabel = text("Pilih Provider", 17, Color.BLACK, true);
        LinearLayout.LayoutParams productLabelLp = wrap();
        productLabelLp.topMargin = dp(20);
        body.addView(productLabel, productLabelLp);

        statusText = text("Masukkan nomor HP terlebih dahulu. Produk akan muncul sesuai prefix nomor.", 13, Color.GRAY, false);
        LinearLayout.LayoutParams statusLp = wrap();
        statusLp.topMargin = dp(7);
        body.addView(statusText, statusLp);

        productContainer = new LinearLayout(this);
        productContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listLp = wrap();
        listLp.topMargin = dp(12);
        body.addView(productContainer, listLp);

        TextView buy = button("LANJUTKAN");
        LinearLayout.LayoutParams buyLp = wrap();
        buyLp.topMargin = dp(18);
        body.addView(buy, buyLp);
        buy.setOnClickListener(v -> processTransaction());

        TextView note = text("Mode uji coba: pesanan belum dikirim ke OtomaX.", 12, Color.GRAY, false);
        LinearLayout.LayoutParams noteLp = wrap();
        noteLp.topMargin = dp(12);
        body.addView(note, noteLp);

        scroll.addView(body);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);

        // Harga sengaja disembunyikan sampai nomor tujuan cukup untuk membaca prefix.
        productContainer.setVisibility(View.GONE);
        numberInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nomor = normalisasiNomor(s.toString());
                selectedProduct = null;
                if (nomor.length() < 4) {
                    daftarProduk.clear();
                    productContainer.removeAllViews();
                    productContainer.setVisibility(View.GONE);
                    statusText.setText("Masukkan minimal 4 angka nomor HP untuk menampilkan produk sesuai operator.");
                    return;
                }
                if (!semuaProduk.isEmpty()) {
                    tampilkanProdukSesuaiPrefix(nomor);
                } else if (!sedangMemuatProduk) {
                    loadProducts(kategoriAktif);
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private String normalisasiNomor(String nomor) {
        if (nomor == null) return "";
        String angka = nomor.replaceAll("[^0-9]", "");
        if (angka.startsWith("62")) angka = "0" + angka.substring(2);
        return angka;
    }

    private boolean prefixCocok(String nomor, JSONObject item) {
        String daftarPrefix = item.optString("prefix_tujuan",
                item.optString("prefix", item.optString("prefix_operator", "")));
        if (daftarPrefix == null || daftarPrefix.trim().isEmpty() || "null".equalsIgnoreCase(daftarPrefix)) {
            return false;
        }
        String[] prefixes = daftarPrefix.split("[,;|\\s]+");
        for (String p : prefixes) {
            String prefix = p.replaceAll("[^0-9]", "");
            if (prefix.startsWith("62")) prefix = "0" + prefix.substring(2);
            if (!prefix.isEmpty() && nomor.startsWith(prefix)) return true;
        }
        return false;
    }

    private void tampilkanProdukSesuaiPrefix(String nomor) {
        if (productContainer == null || statusText == null) return;
        nomorPrefixAktif = nomor;
        selectedProduct = null;
        daftarProduk.clear();
        java.util.LinkedHashMap<String, JSONObject> providers = new java.util.LinkedHashMap<>();
        for (JSONObject item : semuaProduk) {
            String kategoriDb = item.optString("kategori", "").trim();
            if (kategoriDb.equalsIgnoreCase(kategoriAktif) && prefixCocok(nomor, item)) {
                String kodeProvider = item.optString("kode_provider", "").trim();
                if (!kodeProvider.isEmpty() && !providers.containsKey(kodeProvider)) {
                    providers.put(kodeProvider, item);
                }
            }
        }
        productContainer.removeAllViews();
        if (providers.isEmpty()) {
            productContainer.setVisibility(View.GONE);
            statusText.setText("Provider untuk prefix nomor ini belum ditemukan.");
            return;
        }
        productContainer.setVisibility(View.VISIBLE);
        statusText.setText(providers.size() + " provider cocok. Pilih nama provider terlebih dahulu:");
        for (java.util.Map.Entry<String, JSONObject> entry : providers.entrySet()) {
            String kode = entry.getKey();
            String nama = entry.getValue().optString("nama_provider", kode);
            LinearLayout option = new LinearLayout(this);
            option.setOrientation(LinearLayout.VERTICAL);
            option.setPadding(dp(15), dp(15), dp(15), dp(15));
            option.setBackground(round(Color.WHITE, 12));
            option.addView(text(nama, 16, BLUE, true));
            TextView sub = text("Kode provider: " + kode, 12, Color.GRAY, false);
            LinearLayout.LayoutParams subLp = wrap();
            subLp.topMargin = dp(4);
            option.addView(sub, subLp);
            LinearLayout.LayoutParams lp = wrap();
            lp.bottomMargin = dp(9);
            productContainer.addView(option, lp);
            option.setOnClickListener(v -> tampilkanHargaProvider(kode, nama));
        }
    }

    private void tampilkanHargaProvider(String kodeProvider, String namaProvider) {
        providerAktif = kodeProvider;
        selectedProduct = null;
        daftarProduk.clear();
        for (JSONObject item : semuaProduk) {
            if (item.optString("kode_provider", "").equalsIgnoreCase(kodeProvider)
                    && item.optString("kategori", "").equalsIgnoreCase(kategoriAktif)
                    && prefixCocok(nomorPrefixAktif, item)) {
                daftarProduk.add(item);
            }
        }
        productContainer.removeAllViews();
        TextView kembali = text("‹ Kembali ke daftar provider", 14, BLUE, true);
        LinearLayout.LayoutParams backLp = wrap();
        backLp.bottomMargin = dp(12);
        productContainer.addView(kembali, backLp);
        kembali.setOnClickListener(v -> tampilkanProdukSesuaiPrefix(nomorPrefixAktif));
        statusText.setText("Provider: " + namaProvider + " | " + daftarProduk.size() + " produk tersedia");
        if (daftarProduk.isEmpty()) {
            productContainer.addView(text("Belum ada produk aktif untuk provider ini.", 14, Color.GRAY, false));
            return;
        }
        for (JSONObject item : daftarProduk) {
            final JSONObject produk = item;
            String judul = produk.optString("nama_produk", "Produk")
                    + (produk.optString("nominal", "").isEmpty() ? "" : " - " + produk.optString("nominal", ""));
            String harga = formatRupiah(produk.optDouble("harga_jual", 0));
            LinearLayout option = new LinearLayout(this);
            option.setOrientation(LinearLayout.VERTICAL);
            option.setPadding(dp(15), dp(13), dp(15), dp(13));
            option.setBackground(round(Color.WHITE, 12));
            option.addView(text(judul, 15, Color.BLACK, true));
            TextView priceText = text(harga, 14, RED, true);
            LinearLayout.LayoutParams priceLp = wrap();
            priceLp.topMargin = dp(6);
            option.addView(priceText, priceLp);
            LinearLayout.LayoutParams optionLp = wrap();
            optionLp.bottomMargin = dp(9);
            productContainer.addView(option, optionLp);
            option.setOnClickListener(v -> {
                selectedProduct = produk;
                for (int j = 1; j < productContainer.getChildCount(); j++) {
                    productContainer.getChildAt(j).setBackground(round(Color.WHITE, 12));
                }
                option.setBackground(round(Color.rgb(255, 235, 235), 12));
                statusText.setText("Provider: " + namaProvider + " | Dipilih: " + judul + " | " + harga);
            });
        }
    }

    private void loadProducts(String category) {
        sedangMemuatProduk = true;
        statusText.setText("Memuat daftar provider dari OtomaX...");
        if (productContainer != null) {
            productContainer.removeAllViews();
            productContainer.setVisibility(View.GONE);
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(SERVER_URL + "/api/produk");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("X-API-Key", API_KEY);
                int code = conn.getResponseCode();
                java.io.InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream() : conn.getErrorStream();
                if (stream == null) throw new Exception("Server tidak mengirim respons (HTTP " + code + ")");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                if (code != 200) throw new Exception("HTTP " + code + ": " + response);

                JSONObject json = new JSONObject(response.toString());
                JSONArray items = json.optJSONArray("produk");
                if (items == null) throw new Exception("Data 'produk' tidak ditemukan.");
                ArrayList<JSONObject> hasil = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item != null) hasil.add(item);
                }

                runOnUiThread(() -> {
                    sedangMemuatProduk = false;
                    semuaProduk.clear();
                    semuaProduk.addAll(hasil);
                    if (numberInput == null || statusText == null) return;
                    String nomor = normalisasiNomor(numberInput.getText().toString());
                    if (nomor.length() >= 4) {
                        tampilkanProdukSesuaiPrefix(nomor);
                    } else {
                        productContainer.removeAllViews();
                        productContainer.setVisibility(View.GONE);
                        statusText.setText("Masukkan nomor HP untuk menampilkan produk yang sesuai.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    sedangMemuatProduk = false;
                    if (statusText != null) statusText.setText("Gagal memuat produk: " +
                            (e.getMessage() == null ? "Periksa server/API." : e.getMessage()));
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void processTransaction() {
        String number = numberInput == null ? "" : numberInput.getText().toString().trim();
        if (number.isEmpty()) {
            showMessage("Perhatian", "Masukkan nomor tujuan.");
            return;
        }
        if (number.length() < 5 || number.length() > 20) {
            showMessage("Perhatian", "Periksa kembali nomor tujuan.");
            return;
        }
        if (selectedProduct == null) {
            showMessage("Perhatian", "Pilih salah satu produk terlebih dahulu.");
            return;
        }

        String nama = selectedProduct.optString("nama_produk", "Produk");
        String nominal = selectedProduct.optString("nominal", "");
        String kode = selectedProduct.optString("kode_produk", "");
        String harga = formatRupiah(selectedProduct.optDouble("harga_jual", 0));
        String detail = "Nomor tujuan: " + number + "\nProduk: " + nama
                + (nominal.isEmpty() || nominal.equals("null") ? "" : "\nNominal: " + nominal)
                + "\nHarga: " + harga + "\nKode produk: " + kode
                + "\n\nIni hanya konfirmasi simulasi. Belum ada transaksi yang dikirim ke OtomaX.";

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pembelian")
                .setMessage(detail)
                .setNegativeButton("UBAH", null)
                .setPositiveButton("KONFIRMASI", (dialog, which) ->
                        showMessage("Mode Simulasi", "Data pesanan sudah diperiksa.\n\nBelum dikirim ke OtomaX dan saldo tidak dipotong."))
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
