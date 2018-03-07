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

app.controller("basicInfoCtrl", ['$scope', '$http', '$uibModal', '$rootScope', function ($scope, $http, $uibModal, $rootScope) {
    $scope.totalValue = 0.0;

    $scope.calculateTotalValue = function () {
        $scope.totalValue = null;

        for (var holdingList in $rootScope.holdings) {
            for (var holding in $rootScope.holdings[holdingList]) {
                $scope.totalValue += ($rootScope.holdings[holdingList][holding].price *
                    $rootScope.holdings[holdingList][holding].quantity);
            }
        }
    };

    $rootScope.user = null;

    $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
        .then(function (response) {
            console.log(response.data);
            $rootScope.user = response.data;
        });


    $scope.openSettings = function () {
        $uibModal.open({
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
    $rootScope.holdings = {
        cryptos: {},
        stocks: {},
        fiats: {}
    };

    $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
        .then(function (response) {
            $scope.user = response.data;
            console.log(response.data);

            for (var i = 0; i < $scope.user.holdings.length; i++) {
                var holdingType = $scope.user.holdings[i].holdingType;

                if (holdingType === "STOCK") {
                    $rootScope.holdings.stocks[$scope.user.holdings[i].acronym] = $scope.user.holdings[i];
                } else if (holdingType === "CRYPTO") {
                    $rootScope.holdings.cryptos[$scope.user.holdings[i].acronym] = $scope.user.holdings[i];
                } else if (holdingType === "FIAT") {
                    $rootScope.holdings.fiats[$scope.user.holdings[i].acronym] = $scope.user.holdings[i];
                }
            }
        }).then(function () {
            $http.get(
                "https://min-api.cryptocompare.com/data/pricemulti?fsyms="
                + $scope.convertHoldingsToUriVariables($rootScope.holdings.cryptos) + ","
                + $scope.convertHoldingsToUriVariables($rootScope.holdings.fiats)
                + "&tsyms=USD"
            ).then(function (response) {
                for (var holding in $rootScope.holdings.cryptos) {
                    $rootScope.holdings.cryptos[holding].price =
                        (response.data[holding]) ? response.data[holding]["USD"] : null;
                }

                for (var holding in $rootScope.holdings.fiats) {
                    $rootScope.holdings.fiats[holding].price =
                        (response.data[holding]) ? response.data[holding]["USD"] : null;
                }
            });


            //Get all User's stock prices
            $http.get("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=" + $scope.convertHoldingsToUriVariables($scope.holdings.stocks)
                + "&apikey=" + AlphaVantageKey).then(function (response) {

                var i = 0;

                for (var holding in $rootScope.holdings.stocks) {
                    $rootScope.holdings.stocks[holding].price =
                        (response.data["Stock Quotes"][i]["2. price"]) ?
                            response.data["Stock Quotes"][i]["2. price"] : null;
                    i++;
                }
            });
        }
    );

    $scope.convertHoldingsToUriVariables = function (currentHoldingsList) {
        var tickers = "";

        for (var holding in currentHoldingsList) {
            tickers += currentHoldingsList[holding].acronym + ",";
        }

        //Removing trailing comma for uri format
        return tickers.substr(0, tickers.length - 1);
    };

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
    var newHolding = {
        email: $rootScope.user.email,
        name: null,
        acronym: null,
        holdingType: null,
        quantity: -1,
        dateBought: null
    };

    console.log(newHolding);

    $scope.holdingList = [];

    //save reloading every time
    if ($scope.holdingList.length === 0) {
        $http.get(
            "http://localhost:8080/crypto/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
        });

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
        });

        $http.get(
            "http://localhost:8080/stock/list"
        ).then(function (response) {
            $scope.holdingList = $scope.holdingList.concat(response.data);
        });
    }

    $scope.add = function () {
        newHolding.acronym = $scope.holding.acronym;
        newHolding.name = $scope.holding.name;
        newHolding.holdingType = $scope.holding.holdingType;
        newHolding.quantity = $scope.quantity;
        newHolding.dateBought = $scope.holding.dateBought / 1000;

        console.log(newHolding);

        $http({
            method: 'PUT',
            url: "http://localhost:8080/user/add-holding",
            data: newHolding
        }).then(function (response) {
            console.log(response);
            $uibModalStack.dismissAll();
        });
    }
}]);