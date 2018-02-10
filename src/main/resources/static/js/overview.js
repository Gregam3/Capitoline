var app = angular.module("overview", ['ui.bootstrap']);

app.value('Email', 'gregoryamitten@gmail.com');
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

// app.config(['$qProvider', function ($qProvider) {
//     //fixme
//     $qProvider.errorOnUnhandledRejections(false);
// }]);

app.controller("basicInfoCtrl", ['$scope', '$http', '$uibModal', function ($scope, $http, $uibModal) {
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
            templateUrl: 'templates/home/popups/settings-popup.html',
            controller: 'settingsCtrl',
            resolve: {
                items: function () {
                    return $scope.user;
                }
            }
        })
    };
}]);

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', function ($scope, $http, $uibModal) {
    $scope.cryptos = null;

    $scope.addHolding = function () {
        var modalInstance = $uibModal.open({
            templateUrl: 'templates/home/popups/add-holding-popup.html',
            controller: 'addHoldingCtrl',
            resolve: {
                items: function () {
                    return $scope.user;
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

app.controller("addHoldingCtrl", ['$scope', '$http', '$uibModalStack', 'AlphaVantageKey', function ($scope, $http, $uibModalStack, AlphaVantageKey) {
    $scope.holding = {
        name: null,
        acronym: null
    };

    $scope.holdingList = [];


    //save reloading every time
    if ($scope.holdingList.length === 0) {
        $http.get(
            "https://min-api.cryptocompare.com/data/all/coinlist"
        ).then(function (response) {
            $scope.holdingList.push(response.data.Data);
            console.log($scope.holdingList);
        });

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            $scope.holdingList.push(response.data);
            console.log($scope.holdingList);
        });

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            $scope.holdingList.push(response.data);
            console.log($scope.holdingList);
        });
    }

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