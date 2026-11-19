// Customer Portal Logic

let currentAccount = null;
let myTransactions = [];
let allBankAccounts = [];
let isPrivacyMasked = false;

// Always start fresh - clear old browser session cache so no ghost accounts remain
window.onload = function() {
    localStorage.clear();
    currentAccount = null;
    showAuthMode("register");
};

// ==========================================
// 1. AUTH SCREEN MODES (LOGIN / REGISTER / FORGOT)
// ==========================================

function showAuthMode(mode) {
    const regTab = document.getElementById("tab-btn-register");
    const logTab = document.getElementById("tab-btn-login");
    const tabsBar = document.getElementById("auth-tabs-bar");
    const regForm = document.getElementById("form-register");
    const logForm = document.getElementById("form-login");
    const forgotForm = document.getElementById("form-forgot");
    const heading = document.getElementById("auth-heading");
    const subtext = document.getElementById("auth-subtext");

    document.getElementById("auth-screen").style.display = "flex";
    document.getElementById("portal-screen").style.display = "none";

    if (mode === "register") {
        tabsBar.style.display = "grid";
        regTab.classList.add("active");
        logTab.classList.remove("active");
        regForm.style.display = "block";
        logForm.style.display = "none";
        forgotForm.style.display = "none";
        heading.innerText = "Create New Account";
        subtext.innerText = "Open a digital bank account in seconds with an opening deposit.";
    } else if (mode === "login") {
        tabsBar.style.display = "grid";
        logTab.classList.add("active");
        regTab.classList.remove("active");
        logForm.style.display = "block";
        regForm.style.display = "none";
        forgotForm.style.display = "none";
        heading.innerText = "Welcome Back";
        subtext.innerText = "Enter your Account Number and Security PIN to sign in.";
    } else if (mode === "forgot") {
        tabsBar.style.display = "none";
        logForm.style.display = "none";
        regForm.style.display = "none";
        forgotForm.style.display = "block";
        heading.innerText = "Reset Security PIN";
        subtext.innerText = "Verify your registered email address to set a new PIN.";
    }
}

function toggleRegOverdraft() {
    const type = document.getElementById("reg-type").value;
    document.getElementById("reg-od-box").style.display = (type === "CURRENT") ? "block" : "none";
}

// 1A. Handler: Register
function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById("reg-name").value.trim();
    const email = document.getElementById("reg-email").value.trim();
    const pin = document.getElementById("reg-pin").value.trim();
    const type = document.getElementById("reg-type").value;
    const balance = parseFloat(document.getElementById("reg-bal").value);
    const limit = parseFloat(document.getElementById("reg-od").value) || 0;

    if (pin.length < 4) {
        showToast("Security PIN must be at least 4 digits!", true);
        return;
    }

    if (type === "SAVINGS" && balance < 1000) {
        showToast("Savings account requires at least ₹1,000 opening deposit!", true);
        return;
    }

    const payload = { name, email, pin, type, balance, limit };

    fetch("/api/create-account", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (!res.ok) {
            return res.json().then(errData => { throw new Error(errData.message || "Request failed"); });
        }
        return res.json();
    })
    .then(res => {
        if (res.success) {
            showToast("Account Created! Your Account Number is " + res.accNo, false);
            currentAccount = { accNo: res.accNo, name, email, type, balance };
            enterPortal();
        } else {
            showToast(res.message || "Account creation failed!", true);
        }
    })
    .catch(err => showToast(err.message || "Failed to create account", true));
}

// 1B. Handler: Login with Account No & PIN
function handleLogin(e) {
    e.preventDefault();
    const accNo = document.getElementById("login-acc").value.trim().toUpperCase();
    const pin = document.getElementById("login-pin").value.trim();

    if (!accNo || !pin) {
        showToast("Please enter both Account Number and PIN!", true);
        return;
    }

    fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ accNo, pin })
    })
    .then(res => {
        if (!res.ok) {
            return res.json().then(errData => { throw new Error(errData.message || "Login failed"); });
        }
        return res.json();
    })
    .then(res => {
        if (res.success) {
            showToast("Login successful! Welcome " + res.name, false);
            currentAccount = res;
            enterPortal();
        } else {
            showToast(res.message || "Invalid account number or PIN!", true);
        }
    })
    .catch(err => showToast(err.message || "Login failed", true));
}

// 1C. Handler: Reset PIN / Forgot Password
function handleResetPin(e) {
    e.preventDefault();
    const accNo = document.getElementById("forgot-acc").value.trim().toUpperCase();
    const email = document.getElementById("forgot-email").value.trim();
    const newPin = document.getElementById("forgot-new-pin").value.trim();

    if (newPin.length < 4) {
        showToast("New PIN must be at least 4 digits!", true);
        return;
    }

    fetch("/api/reset-pin", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ accNo, email, newPin })
    })
    .then(res => {
        if (!res.ok) {
            return res.json().then(errData => { throw new Error(errData.message || "Reset failed"); });
        }
        return res.json();
    })
    .then(res => {
        if (res.success) {
            showToast("PIN reset successfully! You can now log in.", false);
            e.target.reset();
            showAuthMode("login");
            document.getElementById("login-acc").value = accNo;
            document.getElementById("login-pin").focus();
        } else {
            showToast(res.message || "PIN reset failed!", true);
        }
    })
    .catch(err => showToast(err.message || "Reset failed", true));
}

function logout() {
    localStorage.removeItem("apex_user_acc");
    currentAccount = null;
    showToast("Logged out successfully.", false);
    showAuthMode("login");
}

// ==========================================
// 2. PORTAL DASHBOARD & SECTIONS
// ==========================================

function enterPortal() {
    document.getElementById("auth-screen").style.display = "none";
    document.getElementById("portal-screen").style.display = "block";
    refreshUserData();
}

function refreshUserData() {
    if (!currentAccount) return;

    fetch("/api/accounts")
        .then(res => res.json())
        .then(accounts => {
            allBankAccounts = accounts;
            const updated = accounts.find(a => a.accNo === currentAccount.accNo);
            if (updated) currentAccount = updated;

            // Navbar
            document.getElementById("user-avatar").innerText = currentAccount.name.charAt(0).toUpperCase();
            document.getElementById("user-name-display").innerText = currentAccount.name;
            document.getElementById("user-acc-display").innerText = currentAccount.accNo;

            // Virtual Card
            document.getElementById("card-type-display").innerText = currentAccount.type;
            document.getElementById("card-balance").innerText = "₹" + currentAccount.balance.toLocaleString("en-IN", { minimumFractionDigits: 2 });
            document.getElementById("card-name").innerText = currentAccount.name.toUpperCase();
            document.getElementById("card-acc-num").innerText = currentAccount.accNo;

            // Stats
            document.getElementById("stat-email").innerText = currentAccount.email;
            document.getElementById("stat-rule").innerText = (currentAccount.type === "SAVINGS") ? "Min Bal ₹1,000" : "Overdraft Facility";

            loadMyPassbook();
            renderBeneficiaries();
        });
}

function switchSection(secName) {
    const tabs = document.querySelectorAll(".p-tab");
    tabs.forEach(t => t.classList.remove("active"));
    event.target.classList.add("active");

    const sections = document.querySelectorAll(".portal-section");
    sections.forEach(s => s.classList.remove("active"));
    document.getElementById("sec-" + secName).classList.add("active");

    if (secName === "passbook") {
        loadMyPassbook();
    } else if (secName === "beneficiaries") {
        renderBeneficiaries();
    }
}

function togglePrivacy() {
    isPrivacyMasked = !isPrivacyMasked;
    document.body.classList.toggle("is-private", isPrivacyMasked);
    document.getElementById("eye-icon").innerText = isPrivacyMasked ? "🙈" : "👁️";
    document.getElementById("eye-text").innerText = isPrivacyMasked ? "Show Balance" : "Hide Balance";
}

function setSelfDeposit() {
    if (currentAccount) {
        document.getElementById("deposit-target-acc").value = currentAccount.accNo;
        document.getElementById("deposit-amount").focus();
    }
}

// ==========================================
// 3. CASH DEPOSIT (TARGETS ANY ACCOUNT NUMBER)
// ==========================================
function handlePortalDeposit(e) {
    e.preventDefault();
    const targetAcc = document.getElementById("deposit-target-acc").value.trim().toUpperCase();
    const amount = parseFloat(document.getElementById("deposit-amount").value);

    if (isNaN(amount) || amount <= 0) {
        showToast("Please enter a valid deposit amount greater than 0!", true);
        return;
    }

    const payload = {
        targetAcc: targetAcc,
        amount: amount,
        depositor: currentAccount ? currentAccount.name : "Cash Counter"
    };

    fetch("/api/deposit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast("Deposit of ₹" + amount + " to " + targetAcc + " successful!", false);
            e.target.reset();
            refreshUserData();
        } else {
            showToast(res.message || "Deposit rejected! Please check account number.", true);
        }
    })
    .catch(err => showToast("Deposit error: " + err, true));
}

// ==========================================
// 4. CASH WITHDRAWAL (REQUIRES PIN PASSWORD)
// ==========================================
function handlePortalWithdraw(e) {
    e.preventDefault();
    const amount = parseFloat(document.getElementById("withdraw-amount").value);
    const pin = document.getElementById("withdraw-pin").value.trim();

    if (isNaN(amount) || amount <= 0) {
        showToast("Please enter a valid withdrawal amount!", true);
        return;
    }
    if (!pin) {
        showToast("Security PIN / Password is required to withdraw money!", true);
        return;
    }

    const payload = {
        accNo: currentAccount.accNo,
        amount: amount,
        pin: pin
    };

    fetch("/api/withdraw", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast("Withdrawal of ₹" + amount + " successful!", false);
            e.target.reset();
            refreshUserData();
        } else {
            showToast(res.message || "Withdrawal failed! Check PIN or balance.", true);
        }
    })
    .catch(err => showToast("Withdrawal error: " + err, true));
}

// ==========================================
// 5. INTER-ACCOUNT TRANSFER (REQUIRES PIN)
// ==========================================
function handlePortalTransfer(e) {
    e.preventDefault();
    const target = document.getElementById("transfer-target").value.trim().toUpperCase();
    const amount = parseFloat(document.getElementById("transfer-amt").value);
    const pin = document.getElementById("transfer-pin").value.trim();

    if (target === currentAccount.accNo.toUpperCase()) {
        showToast("Cannot transfer to your own account! Use Deposit instead.", true);
        return;
    }
    if (!pin) {
        showToast("Your Security PIN is required to authorize this transfer!", true);
        return;
    }

    const payload = {
        from: currentAccount.accNo,
        to: target,
        amount: amount,
        pin: pin
    };

    fetch("/api/transfer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(res => {
        if (res.success) {
            showToast("Successfully transferred ₹" + amount + " to " + target + "!", false);
            e.target.reset();
            refreshUserData();
        } else {
            showToast(res.message || "Transfer denied! Check recipient or PIN.", true);
        }
    })
    .catch(err => showToast("Network error: " + err, true));
}

// ==========================================
// 6. PASSBOOK & SAVED BENEFICIARIES
// ==========================================
function loadMyPassbook() {
    fetch("/api/transactions")
        .then(res => res.json())
        .then(txns => {
            myTransactions = txns.filter(t => t.accNo.toUpperCase() === currentAccount.accNo.toUpperCase());
            const tbody = document.getElementById("passbook-tbody");
            if (myTransactions.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No transactions recorded on this account yet.</td></tr>`;
                return;
            }

            const reversed = [...myTransactions].reverse();
            tbody.innerHTML = reversed.map(t => `
                <tr>
                    <td><strong>#${t.id}</strong></td>
                    <td><span class="badge ${t.type.toLowerCase()}">${t.type}</span></td>
                    <td class="masked">₹${t.amount.toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                    <td class="masked">₹${t.balance.toLocaleString("en-IN", { minimumFractionDigits: 2 })}</td>
                    <td>${t.remark}</td>
                </tr>
            `).join("");
        });
}

function getSavedBeneficiaries() {
    if (!currentAccount) return [];
    const raw = localStorage.getItem("apex_bens_" + currentAccount.accNo);
    return raw ? JSON.parse(raw) : [];
}

function saveBeneficiariesList(list) {
    if (!currentAccount) return;
    localStorage.setItem("apex_bens_" + currentAccount.accNo, JSON.stringify(list));
}

function handleAddBeneficiary(e) {
    e.preventDefault();
    const name = document.getElementById("ben-name").value.trim();
    const accNo = document.getElementById("ben-acc").value.trim().toUpperCase();

    if (accNo === currentAccount.accNo.toUpperCase()) {
        showToast("You cannot add your own account as a beneficiary!", true);
        return;
    }

    const list = getSavedBeneficiaries();
    if (list.some(b => b.accNo === accNo)) {
        showToast("This beneficiary is already in your saved list!", true);
        return;
    }

    list.push({ name, accNo });
    saveBeneficiariesList(list);
    showToast("Beneficiary '" + name + "' added successfully!", false);
    e.target.reset();
    renderBeneficiaries();
}

function removeBeneficiary(index) {
    const list = getSavedBeneficiaries();
    const removed = list.splice(index, 1);
    saveBeneficiariesList(list);
    showToast("Removed beneficiary " + (removed[0] ? removed[0].name : ""), false);
    renderBeneficiaries();
}

function renderBeneficiaries() {
    const tbody = document.getElementById("beneficiary-tbody");
    const list = getSavedBeneficiaries();

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="empty-state">No saved beneficiaries yet. Add your first one above!</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map((b, idx) => `
        <tr>
            <td><strong>${b.name}</strong></td>
            <td>${b.accNo}</td>
            <td style="display:flex;gap:8px;">
                <button class="btn-small" onclick="quickSelectTransfer('${b.accNo}')">Transfer</button>
                <button class="btn-small" onclick="quickSelectDeposit('${b.accNo}')">Deposit</button>
                <button class="btn-small" style="color:#ef4444;border-color:#7f1d1d;" onclick="removeBeneficiary(${idx})">Delete</button>
            </td>
        </tr>
    `).join("");
}

function quickSelectTransfer(targetAccNo) {
    switchSection("transfers");
    document.getElementById("transfer-target").value = targetAccNo;
    document.getElementById("transfer-amt").focus();
}

function quickSelectDeposit(targetAccNo) {
    switchSection("deposit-cash");
    document.getElementById("deposit-target-acc").value = targetAccNo;
    document.getElementById("deposit-amount").focus();
}

function showToast(msg, isError) {
    const toast = document.getElementById("toast");
    toast.innerText = msg;
    toast.className = "toast " + (isError ? "error" : "");
    toast.classList.remove("hide");
    setTimeout(() => { toast.classList.add("hide"); }, 4000);
}
