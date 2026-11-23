// Frontend JavaScript written in standard 3rd sem student style (clean DOM + fetch)

let currentUser = null; // store current logged in account
let isEyeClosed = false;

// 1. Check Auth on page load
window.onload = function() {
    // start fresh on every launch as requested
    localStorage.clear();
    setAuthTab("login");
};

// Switch between Login, Register and Forgot PIN tabs
function setAuthTab(tab) {
    let loginForm = document.getElementById("loginForm");
    let registerForm = document.getElementById("registerForm");
    let forgotForm = document.getElementById("forgotForm");
    let btnLogin = document.getElementById("btnTabLogin");
    let btnRegister = document.getElementById("btnTabRegister");

    if (tab === "login") {
        loginForm.style.display = "flex";
        registerForm.style.display = "none";
        forgotForm.style.display = "none";
        btnLogin.className = "tabActive";
        btnRegister.className = "";
    } else if (tab === "register") {
        loginForm.style.display = "none";
        registerForm.style.display = "flex";
        forgotForm.style.display = "none";
        btnLogin.className = "";
        btnRegister.className = "tabActive";
    } else if (tab === "forgot") {
        loginForm.style.display = "none";
        registerForm.style.display = "none";
        forgotForm.style.display = "flex";
        btnLogin.className = "";
        btnRegister.className = "";
    }
}

// Show overdraft input if Current account selected
function checkType() {
    let type = document.getElementById("regType").value;
    let odDiv = document.getElementById("odDiv");
    if (type === "CURRENT") {
        odDiv.style.display = "block";
    } else {
        odDiv.style.display = "none";
    }
}

// Message alert helper
function showMsg(text, isError) {
    let msgBox = document.getElementById("msgBox");
    msgBox.style.display = "block";
    msgBox.innerText = text;
    if (isError) {
        msgBox.className = "msgBox msgError";
    } else {
        msgBox.className = "msgBox msgSuccess";
    }
    setTimeout(function() {
        msgBox.style.display = "none";
    }, 4000);
}

// 2. Authentication: Register
function handleRegister(e) {
    e.preventDefault();
    let name = document.getElementById("regName").value;
    let email = document.getElementById("regEmail").value;
    let pin = document.getElementById("regPin").value;
    let type = document.getElementById("regType").value;
    let balance = document.getElementById("regBal").value;
    let limit = document.getElementById("regOd").value || 0;

    let obj = {
        name: name,
        email: email,
        pin: pin,
        type: type,
        balance: balance,
        limit: limit
    };

    fetch("/api/create-account", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(obj)
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg("Account created! Your Account Number is: " + data.accNo, false);
            // automatically login
            currentUser = {
                accNo: data.accNo,
                name: name,
                email: email,
                type: type,
                balance: parseFloat(balance)
            };
            loadDashboard();
        } else {
            showMsg(data.message || "Failed to create account!", true);
        }
    })
    .catch(function(err) {
        showMsg("Network Error: " + err, true);
    });
}

// 3. Authentication: Login
function handleLogin(e) {
    e.preventDefault();
    let accNo = document.getElementById("loginAcc").value.trim().toUpperCase();
    let pin = document.getElementById("loginPin").value.trim();

    fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ accNo: accNo, pin: pin })
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg("Login successful! Welcome " + data.name, false);
            currentUser = data;
            loadDashboard();
        } else {
            showMsg(data.message || "Invalid Account Number or PIN!", true);
        }
    })
    .catch(function(err) {
        showMsg("Network error during login: " + err, true);
    });
}

// 4. Authentication: Forgot PIN
function handleForgot(e) {
    e.preventDefault();
    let accNo = document.getElementById("forgotAcc").value.trim().toUpperCase();
    let email = document.getElementById("forgotEmail").value.trim();
    let newPin = document.getElementById("forgotNewPin").value.trim();

    fetch("/api/reset-pin", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ accNo: accNo, email: email, newPin: newPin })
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg("PIN reset successful! You can now login.", false);
            setAuthTab("login");
            document.getElementById("loginAcc").value = accNo;
        } else {
            showMsg(data.message || "PIN reset failed!", true);
        }
    })
    .catch(function(err) {
        showMsg("Network error: " + err, true);
    });
}

// 5. Load User Dashboard after login
function loadDashboard() {
    document.getElementById("authSection").style.display = "none";
    document.getElementById("userDashboard").style.display = "block";
    document.getElementById("logoutBtn").style.display = "block";
    document.getElementById("navAccNo").innerText = currentUser.accNo;

    updateUI();
    showSection("dashboard");
}

function updateUI() {
    document.getElementById("dashName").innerText = currentUser.name;
    document.getElementById("dashAccNo").innerText = currentUser.accNo;
    document.getElementById("dashEmail").innerText = currentUser.email;
    document.getElementById("dashType").innerText = currentUser.type;
    document.getElementById("dashBal").innerText = "Rs " + parseFloat(currentUser.balance).toFixed(2);
}

// Section navigation
function showSection(sec) {
    let sections = ["dashboard", "deposit", "withdraw", "transfer", "passbook", "beneficiaries"];
    for (let i = 0; i < sections.length; i++) {
        let el = document.getElementById("sec-" + sections[i]);
        if (el) {
            if (sections[i] === sec) {
                el.style.display = "block";
            } else {
                el.style.display = "none";
            }
        }
    }

    if (sec === "passbook") {
        fetchPassbook();
    } else if (sec === "beneficiaries") {
        renderBeneficiaries();
    }
}

// Eye button to hide / show balance
function toggleEye() {
    isEyeClosed = !isEyeClosed;
    let balText = document.getElementById("dashBal");
    let eyeIcon = document.getElementById("eyeIcon");
    let eyeText = document.getElementById("eyeText");

    if (isEyeClosed) {
        balText.classList.add("masked");
        eyeIcon.className = "bi bi-eye-slash";
        eyeText.innerText = "Show Balance";
    } else {
        balText.classList.remove("masked");
        eyeIcon.className = "bi bi-eye";
        eyeText.innerText = "Hide Balance";
    }
}

// Logout
function handleLogout() {
    currentUser = null;
    document.getElementById("userDashboard").style.display = "none";
    document.getElementById("authSection").style.display = "block";
    document.getElementById("logoutBtn").style.display = "none";
    document.getElementById("navAccNo").innerText = "Not Logged In";
    setAuthTab("login");
    showMsg("Logged out successfully.", false);
}

// Helper to fill self account in deposit
function fillSelfAcc() {
    if (currentUser) {
        document.getElementById("depTargetAcc").value = currentUser.accNo;
    }
}

// 6. Cash Deposit
function handleDeposit(e) {
    e.preventDefault();
    let targetAcc = document.getElementById("depTargetAcc").value.trim().toUpperCase();
    let amt = document.getElementById("depAmt").value;

    fetch("/api/deposit", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ targetAcc: targetAcc, amount: amt, depositor: currentUser.name })
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg(data.message || "Deposit successful!", false);
            if (targetAcc === currentUser.accNo) {
                currentUser.balance = data.balance;
                updateUI();
            }
            e.target.reset();
        } else {
            showMsg(data.message || "Deposit rejected!", true);
        }
    })
    .catch(function(err) {
        showMsg("Error: " + err, true);
    });
}

// 7. Cash Withdraw
function handleWithdraw(e) {
    e.preventDefault();
    let amt = document.getElementById("withAmt").value;
    let pin = document.getElementById("withPin").value;

    fetch("/api/withdraw", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ accNo: currentUser.accNo, amount: amt, pin: pin })
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg(data.message || "Withdrawal successful!", false);
            currentUser.balance = data.balance;
            updateUI();
            e.target.reset();
        } else {
            showMsg(data.message || "Withdrawal failed!", true);
        }
    })
    .catch(function(err) {
        showMsg("Error: " + err, true);
    });
}

// 8. Inter-Account Transfer
function handleTransfer(e) {
    e.preventDefault();
    let toAcc = document.getElementById("transToAcc").value.trim().toUpperCase();
    let amt = document.getElementById("transAmt").value;
    let pin = document.getElementById("transPin").value;

    if (toAcc === currentUser.accNo) {
        showMsg("Cannot transfer to your own account! Use deposit instead.", true);
        return;
    }

    fetch("/api/transfer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ from: currentUser.accNo, to: toAcc, amount: amt, pin: pin })
    })
    .then(function(res) {
        return res.json();
    })
    .then(function(data) {
        if (data.success) {
            showMsg(data.message || "Transfer completed!", false);
            currentUser.balance = currentUser.balance - parseFloat(amt);
            updateUI();
            e.target.reset();
        } else {
            showMsg(data.message || "Transfer failed!", true);
        }
    })
    .catch(function(err) {
        showMsg("Error: " + err, true);
    });
}

// 9. Fetch Passbook Transactions (filter by currentUser)
function fetchPassbook() {
    fetch("/api/transactions")
    .then(function(res) {
        return res.json();
    })
    .then(function(txns) {
        let tbody = document.getElementById("passbookTbody");
        let myTxns = [];

        for (let i = 0; i < txns.length; i++) {
            if (txns[i].accNo.toUpperCase() === currentUser.accNo.toUpperCase()) {
                myTxns.push(txns[i]);
            }
        }

        if (myTxns.length === 0) {
            tbody.innerHTML = "<tr><td colspan='5' style='text-align:center;'>No transactions yet.</td></tr>";
            return;
        }

        let html = "";
        for (let i = myTxns.length - 1; i >= 0; i--) {
            let t = myTxns[i];
            html += "<tr>";
            html += "<td>#" + t.id + "</td>";
            html += "<td><strong>" + t.type + "</strong></td>";
            html += "<td>Rs " + parseFloat(t.amount).toFixed(2) + "</td>";
            html += "<td>Rs " + parseFloat(t.balance).toFixed(2) + "</td>";
            html += "<td>" + t.remark + "</td>";
            html += "</tr>";
        }
        tbody.innerHTML = html;
    });
}

// 10. Beneficiaries (saved in local memory array)
let beneficiariesList = [];

function handleAddBeneficiary(e) {
    e.preventDefault();
    let name = document.getElementById("benName").value.trim();
    let accNo = document.getElementById("benAcc").value.trim().toUpperCase();

    if (accNo === currentUser.accNo) {
        showMsg("You cannot add your own account as a beneficiary!", true);
        return;
    }

    for (let i = 0; i < beneficiariesList.length; i++) {
        if (beneficiariesList[i].accNo === accNo) {
            showMsg("Beneficiary already added!", true);
            return;
        }
    }

    beneficiariesList.push({ name: name, accNo: accNo });
    showMsg("Beneficiary saved!", false);
    e.target.reset();
    renderBeneficiaries();
}

function deleteBeneficiary(index) {
    beneficiariesList.splice(index, 1);
    showMsg("Beneficiary removed.", false);
    renderBeneficiaries();
}

function renderBeneficiaries() {
    let tbody = document.getElementById("benTbody");
    if (beneficiariesList.length === 0) {
        tbody.innerHTML = "<tr><td colspan='3' style='text-align:center;'>No saved beneficiaries yet.</td></tr>";
        return;
    }

    let html = "";
    for (let i = 0; i < beneficiariesList.length; i++) {
        let b = beneficiariesList[i];
        html += "<tr>";
        html += "<td>" + b.name + "</td>";
        html += "<td>" + b.accNo + "</td>";
        html += "<td>";
        html += "<button class='btnBlue' style='padding:4px 8px; font-size:12px; margin-right:5px;' onclick='quickTransfer(\"" + b.accNo + "\")'>Transfer</button>";
        html += "<button class='btnRed' onclick='deleteBeneficiary(" + i + ")'>Delete</button>";
        html += "</td>";
        html += "</tr>";
    }
    tbody.innerHTML = html;
}

function quickTransfer(accNo) {
    showSection("transfer");
    document.getElementById("transToAcc").value = accNo;
}
