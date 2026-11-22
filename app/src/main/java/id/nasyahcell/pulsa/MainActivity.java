
package id.nasyahcell.pulsa;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(22, 103, 190);
    private String selectedCategory = null;

    private EditText numberInput;
    private Spinner productSpinner;
    private TextView statusText;

    private static final int REQUEST_CONTACT = 1001;

    private final String[] categories = {
            "Pulsa",
            "Paket Data",
            "Token PLN",
            "Tagihan PLN",
            "BPJS",
            "PDAM",
            "E-Wallet",
            "Voucher Game",
            "TV Kabel"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private void showHome() {
        selectedCategory = null;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(16), dp(16), dp(20));
        page.setBackgroundColor(Color.rgb(245, 247, 250));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(22), dp(20), dp(22));
        header.setBackground(round(blue, 20));

        TextView title = text("NASYAH CELL", 25, Color.WHITE, true);
        header.addView(title);

        TextView subtitle = text(
                "Layanan Transaksi",
                15,
                Color.WHITE,
                false
        );

        LinearLayout.LayoutParams subLp = matchWrap();
        subLp.topMargin = dp(5);
        header.addView(subtitle, subLp);

        // SALDO DAN DEPOSIT DALAM SATU KOTAK BIRU
        LinearLayout saldoDepositRow = new LinearLayout(this);
        saldoDepositRow.setOrientation(LinearLayout.HORIZONTAL);
        saldoDepositRow.setGravity(Gravity.CENTER_VERTICAL);

        // BAGIAN SALDO
        TextView saldo = text(
                "SALDO ANDA\nRp 0",
                16,
                Color.WHITE,
                true
        );
        saldo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams saldoLp =
                new LinearLayout.LayoutParams(0, dp(55), 1);

        saldoDepositRow.addView(saldo, saldoLp);

        // GARIS PEMISAH PUTIH
        View garis = new View(this);
        garis.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams garisLp =
                new LinearLayout.LayoutParams(dp(1), dp(38));

        saldoDepositRow.addView(garis, garisLp);

        // BAGIAN DEPOSIT
        TextView deposit = text(
                "DEPOSIT /\nISI SALDO",
                14,
                Color.WHITE,
                true
        );

        deposit.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams depositLp =
                new LinearLayout.LayoutParams(0, dp(55), 1);

        saldoDepositRow.addView(deposit, depositLp);

        LinearLayout.LayoutParams rowLp = matchWrap();
        rowLp.topMargin = dp(15);

        header.addView(saldoDepositRow, rowLp);

        deposit.setOnClickListener(v ->
                showMessage(
                        "Menu Deposit",
                        "Fitur deposit akan dihubungkan dengan sistem pembayaran."
                )
        );

        page.addView(header, matchWrap());

        // JUDUL MENU
        TextView menuTitle = text(
                "PILIH LAYANAN",
                18,
                Color.BLACK,
                true
        );

        LinearLayout.LayoutParams menuTitleLp = matchWrap();
        menuTitleLp.topMargin = dp(22);
        menuTitleLp.bottomMargin = dp(12);

        page.addView(menuTitle, menuTitleLp);

        // DAFTAR MENU
        for (int i = 0; i < categories.length; i += 2) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            addCategoryButton(row, categories[i]);

            if (i + 1 < categories.length) {
                addCategoryButton(row, categories[i + 1]);
            }

            LinearLayout.LayoutParams rowLp2 = matchWrap();
            rowLp2.bottomMargin = dp(10);

            page.addView(row, rowLp2);
        }

        // RIWAYAT TRANSAKSI
        TextView history = menuButton("RIWAYAT TRANSAKSI");

        LinearLayout.LayoutParams historyLp = matchWrap();
        historyLp.topMargin = dp(12);

        page.addView(history, historyLp);

        history.setOnClickListener(v ->
                showMessage(
                        "Riwayat Transaksi",
                        "Belum ada transaksi."
                )
        );

        // FOOTER
        TextView footer = text(
                "NASYAH CELL\nLayanan Pulsa dan PPOB",
                13,
                Color.GRAY,
                false
        );

        footer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams footerLp = matchWrap();
        footerLp.topMargin = dp(25);

        page.addView(footer, footerLp);

        scroll.addView(page);
        setContentView(scroll);
    }

    private void addCategoryButton(
            LinearLayout row,
            String category
    ) {

        TextView button = menuButton(category);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, dp(65), 1);

        lp.setMargins(dp(4), dp(4), dp(4), dp(4));

        row.addView(button, lp);

        button.setOnClickListener(v -> {
            selectedCategory = category;
            showProduct(category);
        });
    }

    private void showProduct(String category) {

        selectedCategory = category;

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(20), dp(18), dp(20));
        page.setBackgroundColor(Color.rgb(245, 247, 250));

        TextView title = text(
                category,
                24,
                blue,
                true
        );

        LinearLayout.LayoutParams titleLp = matchWrap();
        titleLp.bottomMargin = dp(20);

        page.addView(title, titleLp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackground(round(Color.WHITE, 16));

        TextView numberLabel = text(
                "Nomor Tujuan",
                15,
                Color.BLACK,
                true
        );

        card.addView(numberLabel);

        // NOMOR TUJUAN DAN KONTAK
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setGravity(Gravity.CENTER_VERTICAL);

        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setHint("Masukkan nomor tujuan");
        numberInput.setInputType(3);
        numberInput.setTextSize(15);
        numberInput.setPadding(dp(10), 0, dp(10), 0);
        numberInput.setBackground(round(
                Color.rgb(245, 247, 250), 10
        ));

        LinearLayout.LayoutParams numberLp =
                new LinearLayout.LayoutParams(0, dp(50), 1);

        numberLp.rightMargin = dp(8);

        contactRow.addView(numberInput, numberLp);

        TextView contactButton = menuButton("Kontak");
        contactButton.setTextSize(13);

        LinearLayout.LayoutParams contactLp =
                new LinearLayout.LayoutParams(dp(85), dp(50));

        contactRow.addView(contactButton, contactLp);

        contactButton.setOnClickListener(v -> pickContact());

        LinearLayout.LayoutParams contactRowLp = matchWrap();
        contactRowLp.topMargin = dp(10);

        card.addView(contactRow, contactRowLp);

        // PILIH PRODUK
        TextView productLabel = text(
                "Pilih Produk",
                15,
                Color.BLACK,
                true
        );

        LinearLayout.LayoutParams productLabelLp = matchWrap();
        productLabelLp.topMargin = dp(20);
        productLabelLp.bottomMargin = dp(8);

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
        card.addView(productSpinner, matchWrap());

        // TOMBOL TRANSAKSI
        TextView buyButton = menuButton("LANJUTKAN TRANSAKSI");

        LinearLayout.LayoutParams buyLp = matchWrap();
        buyLp.topMargin = dp(22);

        card.addView(buyButton, buyLp);

        buyButton.setOnClickListener(v -> processTransaction());

        statusText = text(
                "Status: Siap melakukan transaksi",
                13,
                Color.GRAY,
                false
        );

        LinearLayout.LayoutParams statusLp = matchWrap();
        statusLp.topMargin = dp(15);

        card.addView(statusText, statusLp);

        page.addView(card, matchWrap());

        TextView note = text(
                "Catatan: Ini masih mode prototipe. Transaksi belum dikirim ke OtomaX.",
                12,
                Color.GRAY,
                false
        );

        LinearLayout.LayoutParams noteLp = matchWrap();
        noteLp.topMargin = dp(15);

        page.addView(note, noteLp);

        setContentView(page);
    }

    private String[] getProducts(String category) {

        switch (category) {

            case "Pulsa":
                return new String[]{
                        "Pilih Pulsa",
                        "Pulsa 5.000",
                        "Pulsa 10.000",
                        "Pulsa 15.000",
                        "Pulsa 20.000",
                        "Pulsa 25.000",
                        "Pulsa 50.000",
                        "Pulsa 100.000"
                };

            case "Paket Data":
                return new String[]{
                        "Pilih Paket Data",
                        "Paket Data 1 GB",
                        "Paket Data 2 GB",
                        "Paket Data 3 GB",
                        "Paket Data 5 GB",
                        "Paket Data 10 GB",
                        "Paket Data Unlimited"
                };

            case "Token PLN":
                return new String[]{
                        "Pilih Nominal Token",
                        "Token 20.000",
                        "Token 50.000",
                        "Token 100.000",
                        "Token 200.000",
                        "Token 500.000",
                        "Token 1.000.000"
                };

            case "Tagihan PLN":
                return new String[]{
                        "Pilih Layanan",
                        "Tagihan PLN Pascabayar"
                };

            case "BPJS":
                return new String[]{
                        "Pilih Layanan",
                        "BPJS Kesehatan"
                };

            case "PDAM":
                return new String[]{
                        "Pilih Layanan",
                        "Tagihan PDAM"
                };

            case "E-Wallet":
                return new String[]{
                        "Pilih E-Wallet",
                        "DANA",
                        "OVO",
                        "GoPay",
                        "ShopeePay",
                        "LinkAja"
                };

            case "Voucher Game":
                return new String[]{
                        "Pilih Game",
                        "Mobile Legends",
                        "Free Fire",
                        "PUBG Mobile",
                        "Valorant"
                };

            case "TV Kabel":
                return new String[]{
                        "Pilih Layanan",
                        "IndiHome",
                        "MNC Vision",
                        "Transvision"
                };

            default:
                return new String[]{"Pilih Produk"};
        }
    }

    // MEMBUKA KONTAK HP
    private void pickContact() {

        try {

            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            );

            startActivityForResult(intent, REQUEST_CONTACT);

        } catch (Exception e) {

            showMessage(
                    "Kontak",
                    "Tidak dapat membuka daftar kontak."
            );
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

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
                        null,
                        null,
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {

                    String phone = cursor.getString(0);

                    if (numberInput != null) {
                        numberInput.setText(phone);
                    }
                }

            } catch (Exception e) {

                showMessage(
                        "Kontak",
                        "Gagal mengambil nomor kontak."
                );

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    // PROSES TRANSAKSI
    private void processTransaction() {

        String number = numberInput.getText().toString().trim();

        String product = productSpinner.getSelectedItem().toString();

        if (number.isEmpty()) {

            showMessage(
                    "Perhatian",
                    "Masukkan nomor tujuan terlebih dahulu."
            );

            return;
        }

        if (number.length() < 5 || number.length() > 20) {

            showMessage(
                    "Perhatian",
                    "Periksa kembali nomor tujuan."
            );

            return;
        }

        if (product.startsWith("Pilih")) {

            showMessage(
                    "Perhatian",
                    "Silakan pilih produk terlebih dahulu."
            );

            return;
        }

        statusText.setText(
                "Data siap diproses.\nNomor: " + number
                        + "\nProduk: " + product
                        + "\n\nBelum dikirim ke server OtomaX."
        );
    }

    // TOMBOL MENU
    private TextView menuButton(String value) {

        TextView button = text(
                value,
                15,
                blue,
                true
        );

        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setBackground(round(Color.WHITE, 12));

        return button;
    }

    // FORMAT TEXT
    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    // BACKGROUND BULAT
    private GradientDrawable round(
            int color,
            int radius
    ) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    // LAYOUT
    private LinearLayout.LayoutParams matchWrap() {

        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    // UKURAN DP
    private int dp(int value) {

        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }

    // PESAN
    private void showMessage(
            String title,
            String message
    ) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // TOMBOL KEMBALI HP
    @Override
    public void onBackPressed() {

        if (selectedCategory != null) {

            showHome();

        } else {

            new AlertDialog.Builder(this)
                    .setTitle("Keluar Aplikasi")
                    .setMessage("Yakin ingin keluar dari aplikasi?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton(
                            "Ya",
                            (dialog, which) -> finish()
                    )
                    .show();
        }
    }
}
