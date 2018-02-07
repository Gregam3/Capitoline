var app = angular.module("overview", ['ui.bootstrap']);

app.value('Email', 'gregoryamitten@gmail.com');

app.config(['$qProvider', function ($qProvider) {
    //fixme
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller("basicInfoCtrl", ['$scope', '$http','$uibModal', function ($scope, $http, $uibModal) {
    $scope.conversion = 0;
    $scope.currency = null;
    $scope.userCurrency = null;

    $scope.fetchCurrency = function () {
        $http.get(
            "https://min-api.cryptocompare.com/data/price?" +
            "fsym=" + $scope.currency +
            "&tsyms=" + $scope.userCurrency
        ).then(function (response) {
            console.log(response);
            $scope.conversion = response.data[$scope.userCurrency];
        });
    };

    $scope.openSettings = function () {
        var modalInstance = $uibModal.open({
            templateUrl: 'templates/home/popups/settingsModal.html',
            controller: 'settingsCtrl',
            resolve: {
                items: function () {
                    return $scope.items;
                }
            }
        })
    };
}]);

app.controller("settingsCtrl", ['$scope', '$http', '$uibModalStack', 'Email', function ($scope, $http, $uibModalStack, Email) {
    $scope.user = {
        email: Email,
        settings: {
            currency: null
        }
    };

    $scope.currencies = null;

    $http.get(
        "http://localhost:8080/fiat/list"
    ).then(function (response) {
        console.log(response);
        $scope.currencies = response.data;
    });

    $scope.fetchCurrency = function () {

    };

    $scope.save = function () {
        $http.put(
            "http://localhost:8080/user/update",
            $scope.user,
            {"Content-Type": "application/json"}
        ).then(function (response) {
            console.log(response);
            $uibModalStack.dismissAll();
        });

    }
}]);