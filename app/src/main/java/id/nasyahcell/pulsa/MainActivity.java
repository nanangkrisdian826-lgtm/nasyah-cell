
package id.nasyahcell.pulsa;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    private int blue = Color.rgb(22, 103, 190);
    private LinearLayout page;
    private EditText numberInput;
    private Spinner productSpinner;
    private TextView statusText;
    private String selectedCategory = null;

    private static final int REQUEST_CONTACT = 1001;

    private final String[] categories = {
        "Pulsa", "Paket Data",
        "Token PLN", "Tagihan PLN",
        "BPJS", "PDAM",
        "E-Wallet", "Voucher Game",
        "TV Kabel"
    };

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        getWindow().setStatusBarColor(Color.rgb(15, 76, 145));
        getWindow().setNavigationBarColor(Color.rgb(15, 76, 145));

        showHome();
    }

    @Override
    public void onBackPressed() {

        if (selectedCategory != null) {
            showHome();
            return;
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("NASYAH CELL")
            .setMessage("Yakin ingin keluar dari aplikasi?")
            .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("Ya", (dialog, which) -> finish())
            .show();
    }

    private void showHome() {

        selectedCategory = null;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(245, 248, 252));

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(20), dp(18), dp(25));

        scroll.addView(page);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(22), dp(20), dp(22));
        header.setBackground(round(blue, 20));

        header.addView(text("NASYAH CELL", 25, Color.WHITE, true));
        header.addView(text("Aplikasi Pulsa dan PPOB", 14, Color.WHITE, false));

        TextView saldo = text("SALDO ANDA\nRp 0", 20, Color.WHITE, true);

        LinearLayout.LayoutParams saldoLp = matchWrap();
        saldoLp.topMargin = dp(18);
        header.addView(saldo, saldoLp);

        page.addView(header, matchWrap());

        TextView title = text(
            "Layanan Transaksi", 21,
            Color.rgb(30, 41, 59), true
        );

        LinearLayout.LayoutParams titleLp = matchWrap();
        titleLp.topMargin = dp(25);
        page.addView(title, titleLp);

        page.addView(text(
            "Pilih layanan yang ingin kamu gunakan.",
            13, Color.GRAY, false
        ), matchWrap());

        for (int i = 0; i < categories.length; i += 2) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams rowLp = matchWrap();
            rowLp.topMargin = dp(12);

            page.addView(row, rowLp);

            addMenu(row, categories[i]);

            if (i + 1 < categories.length) {
                addMenu(row, categories[i + 1]);
            }
        }

        TextView deposit = menuButton("DEPOSIT / ISI SALDO");

        LinearLayout.LayoutParams depositLp = matchWrap();
        depositLp.topMargin = dp(18);
        page.addView(deposit, depositLp);

        deposit.setOnClickListener(v ->
            showMessage("Menu Deposit",
                "Fitur deposit akan dihubungkan dengan sistem pembayaran.")
        );

        TextView history = menuButton("RIWAYAT TRANSAKSI");

        LinearLayout.LayoutParams historyLp = matchWrap();
        historyLp.topMargin = dp(10);
        page.addView(history, historyLp);

        history.setOnClickListener(v ->
            showMessage("Riwayat",
                "Riwayat transaksi akan tersedia setelah server API terhubung.")
        );

        page.addView(text(
            "NASYAH CELL © 2026", 12, Color.GRAY, false
        ), matchWrap());

        setContentView(scroll);
    }

    private void addMenu(LinearLayout row, String name) {

        TextView button = menuButton(name);

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(0, dp(76), 1);

        lp.setMargins(dp(4), 0, dp(4), 0);

        row.addView(button, lp);

        button.setOnClickListener(v -> showProduct(name));
    }

    private TextView menuButton(String name) {

        TextView button = text(name, 15, blue, true);

        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(5), dp(8), dp(5), dp(8));
        button.setBackground(round(Color.WHITE, 15));

        return button;
    }

    private void showProduct(String category) {

        selectedCategory = category;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(245, 248, 252));

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(22), dp(20), dp(25));

        scroll.addView(form);

        form.addView(text(
            category, 25, Color.rgb(30, 41, 59), true
        ), matchWrap());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(Color.WHITE, 18));

        LinearLayout.LayoutParams cardLp = matchWrap();
        cardLp.topMargin = dp(20);
        form.addView(card, cardLp);

        String inputLabel = category.equals("Token PLN")
                || category.equals("Tagihan PLN")
                || category.equals("BPJS")
                || category.equals("PDAM")
                || category.equals("TV Kabel")
                ? "Nomor ID Pelanggan"
                : "Nomor HP / Nomor Tujuan";

        card.addView(text(inputLabel, 14, Color.DKGRAY, true));

        // Kolom nomor dan tombol kontak
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams contactRowLp = matchWrap();
        contactRowLp.topMargin = dp(8);
        card.addView(contactRow, contactRowLp);

        numberInput = new EditText(this);
        numberInput.setSingleLine(true);
        numberInput.setTextSize(16);
        numberInput.setHint("Masukkan nomor tujuan");
        numberInput.setInputType(3);

        LinearLayout.LayoutParams inputLp =
            new LinearLayout.LayoutParams(0, -2, 1);

        contactRow.addView(numberInput, inputLp);

        Button contactButton = new Button(this);
        contactButton.setText("Kontak");
        contactButton.setTextColor(Color.WHITE);
        contactButton.setAllCaps(false);
        contactButton.setTextSize(13);
        contactButton.setBackground(round(blue, 10));

        LinearLayout.LayoutParams contactLp =
            new LinearLayout.LayoutParams(-2, dp(48));

        contactLp.leftMargin = dp(8);

        contactRow.addView(contactButton, contactLp);

        contactButton.setOnClickListener(v -> pickContact());

        TextView productLabel = text(
            "Pilih Produk", 14, Color.DKGRAY, true
        );

        LinearLayout.LayoutParams labelLp = matchWrap();
        labelLp.topMargin = dp(20);
        card.addView(productLabel, labelLp);

        productSpinner = new Spinner(this);

        String[] products = getProducts(category);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            products
        );

        productSpinner.setAdapter(adapter);
        card.addView(productSpinner, matchWrap());

        Button buy = new Button(this);
        buy.setText("LANJUTKAN TRANSAKSI");
        buy.setTextColor(Color.WHITE);
        buy.setAllCaps(false);
        buy.setTextSize(15);
        buy.setBackground(round(blue, 14));

        LinearLayout.LayoutParams buyLp = matchWrap();
        buyLp.topMargin = dp(24);
        card.addView(buy, buyLp);

        statusText = text(
            "Transaksi belum dikirim ke server.",
            13, Color.GRAY, false
        );

        LinearLayout.LayoutParams statusLp = matchWrap();
        statusLp.topMargin = dp(15);
        card.addView(statusText, statusLp);

        buy.setOnClickListener(v -> processTransaction());

        form.addView(text(
            "Catatan: Ini masih mode prototipe. Transaksi belum dikirim ke OtomaX.",
            12, Color.GRAY, false
        ), matchWrap());

        setContentView(scroll);
    }

    // Membuka daftar kontak HP
    private void pickContact() {

        Intent intent = new Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        );

        startActivityForResult(intent, REQUEST_CONTACT);
    }

    // Mengambil nomor dari kontak yang dipilih
    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
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
                    null,
                    null,
                    null
                );

                if (cursor != null && cursor.moveToFirst()) {

                    int index = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    );

                    if (index >= 0) {
                        String phone = cursor.getString(index);

                        if (phone != null) {
                            numberInput.setText(phone.trim());
                            numberInput.setSelection(
                                numberInput.getText().length()
                            );
                        }
                    }
                }

            } catch (Exception e) {
                showMessage(
                    "Kontak",
                    "Tidak dapat membaca nomor kontak."
                );
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private String[] getProducts(String category) {

        switch (category) {

            case "Pulsa":
                return new String[]{
                    "Pilih Nominal",
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
                    "Pilih Operator / Paket",
                    "Telkomsel",
                    "Indosat",
                    "XL",
                    "Axis",
                    "Tri",
                    "Smartfren"
                };

            case "Token PLN":
                return new String[]{
                    "Pilih Nominal",
                    "Token 20.000",
                    "Token 50.000",
                    "Token 100.000",
                    "Token 200.000",
                    "Token 500.000",
                    "Token 1.000.000"
                };

            case "Tagihan PLN":
                return new String[]{"Tagihan PLN Pascabayar"};

            case "BPJS":
                return new String[]{"Tagihan BPJS Kesehatan"};

            case "PDAM":
                return new String[]{"Tagihan PDAM"};

            case "E-Wallet":
                return new String[]{
                    "Pilih E-Wallet",
                    "DANA",
                    "OVO",
                    "GoPay",
                    "ShopeePay"
                };

            case "Voucher Game":
                return new String[]{
                    "Pilih Game",
                    "Mobile Legends",
                    "Free Fire",
                    "PUBG Mobile",
                    "Google Play"
                };

            case "TV Kabel":
                return new String[]{"Tagihan TV Kabel"};

            default:
                return new String[]{"Pilih Produk"};
        }
    }

    private void processTransaction() {

        String number = numberInput.getText().toString().trim();
        int selected = productSpinner.getSelectedItemPosition();

        if (number.length() < 5 || number.length() > 20) {
            numberInput.setError("Periksa nomor tujuan.");
            return;
        }

        if (selected == 0 &&
            (selectedCategory.equals("Pulsa")
            || selectedCategory.equals("Paket Data")
            || selectedCategory.equals("Token PLN")
            || selectedCategory.equals("E-Wallet")
            || selectedCategory.equals("Voucher Game"))) {

            statusText.setText("Silakan pilih produk terlebih dahulu.");
            return;
        }

        String product = productSpinner.getSelectedItem().toString();

        statusText.setText(
            "Data siap: " + selectedCategory
            + "\nTujuan: " + number
            + "\nProduk: " + product
            + "\n\nBelum dikirim ke OtomaX. Menunggu integrasi API."
        );
    }

    private void showMessage(String title, String message) {

        new android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private TextView text(
        String value,
        int size,
        int color,
        boolean bold
    ) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return t;
    }

    private GradientDrawable round(int color, int radius) {

        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));

        return d;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(int value) {
        return (int) (
            value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}
