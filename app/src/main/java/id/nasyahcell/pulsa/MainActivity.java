
private final int PANEL = Color.rgb(134, 164, 173);

private void showLogin() {
    accountScreen = false;
    depositScreen = false;
    selectedCategory = null;

    LinearLayout page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setGravity(Gravity.CENTER);
    page.setPadding(dp(24), dp(20), dp(24), dp(20));
    page.setBackgroundColor(DARK);

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setGravity(Gravity.CENTER_HORIZONTAL);
    card.setPadding(dp(18), dp(25), dp(18), dp(25));
    card.setBackground(round(PANEL, 20));

    TextView title = text("LOGIN", 28, DARK, true);
    title.setGravity(Gravity.CENTER);
    card.addView(title);

    TextView logo = text("NASYAH\nCELL", 27, RED, true);
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

    masuk.setOnClickListener(v ->
        prosesAuth("/api/login",
            kode.getText().toString().trim(),
            password.getText().toString(), "", "")
    );

    TextView daftar = text(
        "Belum punya akun? Daftar sekarang!",
        14, DARK, true
    );
    daftar.setGravity(Gravity.CENTER);

    LinearLayout.LayoutParams daftarLp = wrap();
    daftarLp.topMargin = dp(22);
    card.addView(daftar, daftarLp);

    daftar.setOnClickListener(v -> showRegister());

    TextView footer = text(
        "NASYAH CELL",
        16, DARK, true
    );
    footer.setGravity(Gravity.CENTER);

    LinearLayout.LayoutParams footerLp = wrap();
    footerLp.topMargin = dp(30);
    card.addView(footer, footerLp);

    scroll.addView(card);
    page.addView(scroll, new LinearLayout.LayoutParams(-1, -1));

    setContentView(page);
}

private void showRegister() {
    LinearLayout page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setGravity(Gravity.CENTER);
    page.setPadding(dp(24), dp(20), dp(24), dp(20));
    page.setBackgroundColor(DARK);

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setGravity(Gravity.CENTER_HORIZONTAL);
    card.setPadding(dp(18), dp(25), dp(18), dp(25));
    card.setBackground(round(PANEL, 20));

    TextView title = text(
        "PENDAFTARAN",
        27, DARK, true
    );
    title.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams titleLp = wrap();
    titleLp.bottomMargin = dp(30);
    card.addView(title, titleLp);

    EditText kode = authInput("Kode Reseller OtomaX");
    card.addView(kode, authFieldLp());

    EditText aktivasi = authInput("Kode Aktivasi dari Admin");
    card.addView(aktivasi, authFieldLp());

    EditText password = authInput("Buat Password");
    password.setInputType(129);
    card.addView(password, authFieldLp());

    EditText konfirmasi = authInput("Ulangi Password");
    konfirmasi.setInputType(129);
    card.addView(konfirmasi, authFieldLp());

    TextView daftar = button("KIRIM");
    LinearLayout.LayoutParams daftarLp = wrap();
    daftarLp.topMargin = dp(12);
    card.addView(daftar, daftarLp);

    daftar.setOnClickListener(v ->
        prosesAuth("/api/daftar",
            kode.getText().toString().trim(),
            password.getText().toString(),
            konfirmasi.getText().toString(),
            aktivasi.getText().toString().trim())
    );

    TextView kembali = text(
        "←  KEMBALI",
        15, RED, true
    );
    kembali.setGravity(Gravity.CENTER);
    kembali.setPadding(dp(15), dp(15), dp(15), dp(15));
    kembali.setBackground(round(Color.WHITE, 8));

    LinearLayout.LayoutParams kembaliLp = new LinearLayout.LayoutParams(
        dp(150), -2
    );
    kembaliLp.gravity = Gravity.CENTER;
    kembaliLp.topMargin = dp(20);
    card.addView(kembali, kembaliLp);

    kembali.setOnClickListener(v -> showLogin());

    TextView footer = text(
        "NASYAH CELL",
        16, DARK, true
    );
    footer.setGravity(Gravity.CENTER);

    LinearLayout.LayoutParams footerLp = wrap();
    footerLp.topMargin = dp(25);
    card.addView(footer, footerLp);

    scroll.addView(card);
    page.addView(scroll, new LinearLayout.LayoutParams(-1, -1));

    setContentView(page);
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
