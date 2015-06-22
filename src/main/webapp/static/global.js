function checkAll(form, name, checked) {
    if (form) {
        var element = form[name];
        if (element) {
            if (element.length) {
                for (var i = 0; i < element.length; i++) {
                    if (!element[i].disabled) {
                        element[i].checked = checked;
                    }
                }
            } else {
                if (!element.disabled) {
                    element.checked = checked;
                }
            }
        }
    }
}

function isChecked(form, name) {
    var checked = false;
    var element = form[name];
    if (element) {
        if (element.length) {
            for (var i = 0; i < element.length; i++) {
                if (element[i].checked) {
                    checked = true;
                    break;
                }
            }
        } else {
            checked = element.checked;
        }
    }
    return checked;
}