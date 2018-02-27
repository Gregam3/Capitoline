var app = angular.module("overview", ['ui.bootstrap', 'smart-table']);
// app.config(['$qProvider', function ($qProvider) {
//     $qProvider.errorOnUnhandledRejections(false);
// }]);

angular.module("overview").factory('user', function ($http) {
    return null;
    $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
        .then(function (response) {
            console.log(response.data);
            return response.data;
        });
});

app.value('Email', 'gregoryamitten@gmail.com');
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

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

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey) {
    $scope.user = null;
    $scope.holdings = {
        cryptos: [],
        stocks: [],
        fiats: []
    };

    $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
        .then(function (response) {
            console.log(response.data);
            $scope.user = response.data;

            console.log($scope.user.holdings.length);

            for (var i = 0; i < $scope.user.holdings.length; i++) {
                var holdingType = $scope.user.holdings[i].holdingType;

                console.log(holdingType);

                if (holdingType === "STOCK") {
                    $scope.holdings.stocks.push($scope.user.holdings[i]);
                } else if (holdingType === "CRYPTO") {
                    $scope.holdings.cryptos.push($scope.user.holdings[i]);
                } else if (holdingType === "FIAT") {
                    $scope.holdings.fiats.push($scope.user.holdings[i]);
                }
            }

            console.log($scope.holdings);
        }).then(function () {
            var stockTickers = "";

            for (var i = 0; i < $scope.holdings.stocks.length; i++) {
                    stockTickers += $scope.user.holdings[i].acronym + ",";
            }

            //Removing trailing comma for uri format
            stockTickers = stockTickers.substr(0, stockTickers.length - 1);

            //Get all User's stock prices
            $http.get("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=" + stockTickers
                + "&apikey=" + AlphaVantageKey).then(function (response) {
                console.log(response.data);
                for (var i = 0; i < $scope.holdings.stocks.length; i++) {
                    console.log(response.data["Stock Quotes"][i]["2. price"]);
                    $scope.holdings.stocks[i].price = response.data["Stock Quotes"][i]["2. price"];
                }
                console.log($scope.holdings);
            })
        }
    );

    $scope.addHolding = function () {
        $rootScope.addHoldingModal = $uibModal.open({
            templateUrl: 'templates/home/popups/add-holding-popup.html',
            controller: 'addHoldingCtrl'
        }).result.then(function (user) {
        });
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
            JSON.stringify($scope.user),
            {"Content-Type": "application/json"}
        ).then(function (response) {
            console.log(response);
            $uibModalStack.dismissAll();
        });

    }
}]);

app.controller("addHoldingCtrl", ['$scope', '$http', '$uibModalStack', 'user', '$rootScope', function ($scope, $http, $uibModalStack, user, $rootScope) {
    $scope.holding = {
        name: null,
        acronym: null,
        quantity: 0
    };

    var user1 = null;

    $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
        .then(function (response) {
            console.log(response.data);
            user1 = response.data;
        });

    $scope.holdingList = [];


    //save reloading every time
    if ($scope.holdingList.length === 0) {
        $http.get(
            "http://localhost:8080/crypto/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
            console.log(response.data);
        });

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
            console.log(response.data);
        });

        $http.get(
            "http://localhost:8080/stock/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
            console.log(response.data);
        });
    }

    $scope.add = function () {
        console.log($scope.holding);
        user1.holdings.push($scope.holding);

        $http({
            method: 'PUT',
            url: "http://localhost:8080/user/update",
            data: user1
        }).then(function (response) {
            console.log(response);
            $uibModalStack.dismissAll();
        });
    }
}]);