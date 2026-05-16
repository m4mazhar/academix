// app.js - Academix Coaching Center Management System

document.addEventListener('DOMContentLoaded', function() {
    initializeTooltips();
    initializePopovers();
    setupFormValidation();
});

// Bootstrap tooltips
function initializeTooltips() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Bootstrap popovers
function initializePopovers() {
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

// Form validation
function setupFormValidation() {
    var forms = document.querySelectorAll('form[novalidate]');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-BD', {
        style: 'currency',
        currency: 'BDT',
        minimumFractionDigits: 0
    }).format(amount);
}

// Show loading indicator
function showLoading(element) {
    element.classList.add('loading');
    element.disabled = true;
}

// Hide loading indicator
function hideLoading(element) {
    element.classList.remove('loading');
    element.disabled = false;
}

// AJAX GET helper
function fetchData(url) {
    return fetch(url).then(response => response.json());
}

// AJAX POST helper
function submitForm(formElement) {
    const formData = new FormData(formElement);
    return fetch(formElement.action, {
        method: formElement.method || 'POST',
        body: new URLSearchParams(formData)
    });
}

// Auto-dismiss alerts after 5 seconds
document.querySelectorAll('.alert').forEach(function(alert) {
    if (alert.classList.contains('alert-dismissible')) {
        setTimeout(function() {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    }
});
