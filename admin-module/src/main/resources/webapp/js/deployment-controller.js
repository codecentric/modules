let IndexController = (function () {

    const REFRESH_WAIT = 1000; // ms

    let listModules = function () {
        $.get(Constants.ModuleApiPath, function (data) {
            Utilities.SortByModuleName(data.modules);
            ModulesTableRenderer.Render(data.modules);
        });
    };

    let deployModule = function (formData) {
        $.ajax({
            url: Constants.ModuleDeployApiPath,
            data: formData,
            processData: false,
            contentType: false,
            type: 'POST',
            success: function (response) {

                Messages.Success('Module deployed');

                clearTableBody();
                showSpinner();

                setTimeout(function () {
                    clearTableBody();
                    listModules();
                }, REFRESH_WAIT);

            },
            error: function (error) {
                Messages.Error('Module could not be deployed: ' + error.responseText);
            }
        });
    };

    let updateModule = function (moduleName, moduleFilePath) {
        let body = JSON.stringify({moduleFilePath: moduleFilePath});
        $.ajax({
            url: Constants.ModuleApiPath,
            type: 'PUT',
            contentType: "application/json",
            data: body,
            success: function (result) {

                Messages.Success('Module "' + moduleName + '" updated');

                clearTableBody();
                showSpinner();

                setTimeout(function () {
                    clearTableBody();
                    listModules();
                }, REFRESH_WAIT);

            },
            error: function (error) {
                Messages.Error('Module "' + moduleName + '" could not be updated: ' + error.responseText);
            }
        });
    };

    let removeModule = function (moduleName, moduleFilePath) {
        let body = JSON.stringify({moduleFilePath: moduleFilePath});
        $.ajax({
            url: Constants.ModuleApiPath,
            type: 'DELETE',
            contentType: "application/json",
            data: body,
            success: function (result) {

                Messages.Success('Module "' + moduleName + '" removed');

                clearTableBody();
                showSpinner();

                setTimeout(function () {
                    clearTableBody();
                    listModules();
                }, REFRESH_WAIT);

            },
            error: function (error) {
                Messages.Error('Module "' + moduleName + '" could not be removed: ' + error.responseText);
            }
        });
    };

    let clearTableBody = function () {
        $("#deployed-modules tbody tr").remove();
    };

    let showSpinner = function () {
        let table = document.getElementById("deployed-modules");
        let tableBody = $(table).find('tbody');
        let content = Template.Row(Template.Column('<p class="refreshing-list">' + Template.Bold('Updating modules &hellip;') + '</p>', '', 8));
        tableBody.append(content);
    };

    return {

        ListModules: listModules,

        DeployModule: deployModule,

        UpdateModule: updateModule,

        RemoveModule: removeModule

    }

})();