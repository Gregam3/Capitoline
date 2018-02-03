var app = angular.module("overview", []);

app.controller("basicInfoCtrl", function ($scope, $http) {
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
    }
});

app.controller("settingsCtrl", function ($scope, $http) {
    $scope.fetchCurrency = function () {
        $http.get(
            "localhost:8080/"
        ).then(function (response) {
            console.log(response);
            $scope.conversion = response.data[$scope.userCurrency];
        });
    }
});