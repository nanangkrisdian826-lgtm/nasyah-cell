
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

public class MainActivity extends Activity {

    private final int RED = Color.rgb(218, 43, 48);
    private final int DARK = Color.rgb(5, 62, 78);
    private final int BG = Color.rgb(245, 246, 248);
    private final int BLUE = Color.rgb(20, 105, 180);

    private LinearLayout root;
    private LinearLayout content;
    private TextView balanceText;

    private EditText numberInput;
    private Spinner productSpinner;
    private TextView statusText;

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

        refresh.setOnClickListener(v -> showMessage(
                "Refresh", "Saldo dan data akan diperbarui setelah terhubung ke server."
        ));

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
            balanceText.setText(saldoVisible ? "Rp 0" : "******");
        });

        saldoCard.addView(saldoTop);

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(210, 220, 225));
        saldoCard.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout saldoBottom = new LinearLayout(this);
        saldoBottom.setGravity(Gravity.CENTER);
        saldoBottom.setOrientation(LinearLayout.HORIZONTAL);

        TextView saldoAmount = text("💰  0", 17, Color.BLACK, false);
        saldoAmount.setGravity(Gravity.CENTER);

        TextView deposit = text("➕ Isi Saldo", 17, Color.BLACK, false);
        deposit.setGravity(Gravity.CENTER);

        saldoBottom.addView(saldoAmount,
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

        TextView productLabel = text(
                "Pilih Produk", 15, Color.BLACK, true
        );
        LinearLayout.LayoutParams productLabelLp = wrap();
        productLabelLp.topMargin = dp(20);
        card.addView(productLabel, productLabelLp);

        productSpinner = new Spinner(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getProducts(category)
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        productSpinner.setAdapter(adapter);
        card.addView(productSpinner);

        TextView buy = button("LANJUTKAN TRANSAKSI");
        LinearLayout.LayoutParams buyLp = wrap();
        buyLp.topMargin = dp(20);
        card.addView(buy, buyLp);

        statusText = text(
                "Status: Siap melakukan transaksi",
                13, Color.GRAY, false
        );

        LinearLayout.LayoutParams statusLp = wrap();
        statusLp.topMargin = dp(15);
        card.addView(statusText, statusLp);

        page.addView(card);

        TextView note = text(
                "Transaksi belum terhubung ke server OtomaX.",
                12, Color.GRAY, false
        );
        LinearLayout.LayoutParams noteLp = wrap();
        noteLp.topMargin = dp(15);
        page.addView(note, noteLp);

        setContentView(page);

        buy.setOnClickListener(v -> processTransaction());
    }

    private String[] getProducts(String category) {
        switch (category) {
            case "Pulsa":
                return new String[]{
                        "Pilih Pulsa", "Pulsa 5.000", "Pulsa 10.000",
                        "Pulsa 15.000", "Pulsa 20.000",
                        "Pulsa 25.000", "Pulsa 50.000",
                        "Pulsa 100.000"
                };

            case "Paket Data":
                return new String[]{
                        "Pilih Paket Data", "Paket Data 1 GB",
                        "Paket Data 2 GB", "Paket Data 3 GB",
                        "Paket Data 5 GB", "Paket Data 10 GB"
                };

            case "Token PLN":
                return new String[]{
                        "Pilih Token", "Token 20.000",
                        "Token 50.000", "Token 100.000",
                        "Token 200.000", "Token 500.000"
                };

            case "Telepon & SMS":
                return new String[]{
                        "Pilih Produk", "Paket Telepon",
                        "Paket SMS"
                };

            case "Voucher Data":
                return new String[]{
                        "Pilih Voucher", "Voucher Data 1 GB",
                        "Voucher Data 2 GB", "Voucher Data 5 GB"
                };

            case "Masa Aktif":
                return new String[]{
                        "Pilih Masa Aktif", "Masa Aktif 7 Hari",
                        "Masa Aktif 30 Hari"
                };

            case "Paket Terbaik":
                return new String[]{
                        "Pilih Paket", "Paket Internet",
                        "Paket Combo"
                };

            case "Game":
                return new String[]{
                        "Pilih Game", "Mobile Legends",
                        "Free Fire", "PUBG Mobile",
                        "Valorant"
                };

            case "Kirim Uang":
                return new String[]{
                        "Pilih Layanan", "Transfer Uang"
                };

            case "Kartu Perdana":
                return new String[]{
                        "Pilih Kartu", "Kartu Perdana"
                };

            case "Layanan Digital":
                return new String[]{
                        "Pilih Layanan", "Layanan Digital"
                };

            case "Tagihan PLN":
                return new String[]{
                        "Pilih Layanan", "PLN Pascabayar"
                };

            default:
                return new String[]{"Pilih Produk"};
        }
    }

    private void processTransaction() {
        String number = numberInput.getText().toString().trim();
        String product = productSpinner.getSelectedItem().toString();

        if (number.isEmpty()) {
            showMessage("Perhatian", "Masukkan nomor tujuan.");
            return;
        }

        if (number.length() < 5 || number.length() > 20) {
            showMessage("Perhatian", "Periksa kembali nomor tujuan.");
            return;
        }

        if (product.startsWith("Pilih")) {
            showMessage("Perhatian", "Silakan pilih produk.");
            return;
        }

        statusText.setText(
                "Data siap diproses\nNomor: " + number
                        + "\nProduk: " + product
                        + "\n\nBelum dikirim ke OtomaX."
        );
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
