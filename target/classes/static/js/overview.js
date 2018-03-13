var app = angular.module("overview", ['ui.bootstrap', 'smart-table', 'n3-line-chart']);
angular.module("overview").factory('user', function ($http) {
    return null;
});

app.value('Email', 'gregoryamitten@gmail.com');
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

app.controller("basicInfoCtrl", ['$scope', '$http', '$uibModal', '$rootScope', function ($scope, $http, $uibModal, $rootScope) {
    $rootScope.totalValue = 0.0;
    $rootScope.acquisitionCost = 0.0;
    $rootScope.historicalPortfolio = {dataSet: []};
    $rootScope.user = null;

    $scope.options = {
        series: [
            {
                axis: "y",
                dataset: "dataSet",
                key: "value",
                label: "Portfolio Value ($)",
                color: "#ffa500",
                type: ['line', 'area'],
                id: 'mySeries0'
            }
        ],
        axes: {
            x: {
                key: "time",
                type: 'date',
                tickFormat: d3.time.format("%b %y")
            },
            y: {
                type: 'currency'
            }
        }
    };


    //Fetch and process graph data
    $rootScope.generateGraph = function () {
        $http.get(
            'http://localhost:8080/user/get/holding-graph-data/gregoryamitten@gmail.com'
        ).then(function (response) {
            $rootScope.historicalPortfolio.dataSet = response.data.Total;
            console.log(response.data);
        });

        console.log($rootScope.historicalPortfolio);
    }
}]);

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey) {
    $scope.user = null;

    $rootScope.holdings = {
        cryptos: {},
        stocks: {},
        fiats: {}
    };

    $rootScope.updateUser = function () {

        $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
            .then(function (response) {
                $scope.user = response.data;
                console.log(response.data);

                for (let i = 0; i < $scope.user.holdings.length; i++) {
                    const currentHolding = $scope.user.holdings[i];

                    if (currentHolding.holdingType === "STOCK") {
                        $rootScope.holdings.stocks[currentHolding.acronym] = currentHolding;
                    } else if (currentHolding.holdingType === "CRYPTO") {
                        $rootScope.holdings.cryptos[currentHolding.acronym] = currentHolding;
                    } else if (currentHolding.holdingType === "FIAT") {
                        $rootScope.holdings.fiats[currentHolding.acronym] = currentHolding;
                    }

                    for (let i = 0; i < currentHolding.transactions.length; i++) {
                        $rootScope.acquisitionCost +=
                            currentHolding.transactions[i].price * currentHolding.transactions[i].quantity;
                    }

                    $rootScope.generateGraph();
                }
            }).then(function () {
                $http.get(
                    "https://min-api.cryptocompare.com/data/pricemulti?fsyms="
                    + $scope.convertHoldingsToUriVariables($rootScope.holdings.cryptos) + ","
                    + $scope.convertHoldingsToUriVariables($rootScope.holdings.fiats)
                    + "&tsyms=USD"
                ).then(function (response) {
                    let holding;
                    for (holding in $rootScope.holdings.cryptos) {
                        $rootScope.holdings.cryptos[holding].price =
                            (response.data[holding]) ? response.data[holding]["USD"] : null;
                        console.log($rootScope.holdings.cryptos[holding].price)
                        $rootScope.totalValue +=
                            $rootScope.holdings.cryptos[holding].price * $rootScope.holdings.cryptos[holding].totalQuantity;
                    }

                    for (holding in $rootScope.holdings.fiats) {
                        $rootScope.holdings.fiats[holding].price =
                            (response.data[holding]) ? response.data[holding]["USD"] : null;
                        console.log($rootScope.holdings.fiats[holding].price);
                        $rootScope.totalValue +=
                            $rootScope.holdings.fiats[holding].price * $rootScope.holdings.fiats[holding].totalQuantity;
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
                        $rootScope.totalValue +=
                            $rootScope.holdings.stocks[holding].price * $rootScope.holdings.stocks[holding].totalQuantity;
                        i++;
                    }
                });
            }
        );
    };

    $rootScope.updateUser();


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

    $scope.removeHolding = function (acronym, holdingType) {
        $http.delete('http://localhost:8080/user/delete/holding/' + acronym + '/' + holdingType + '/' + this.amountToRemove)
            .then(function (response) {
                console.log(response.data);
                $rootScope.updateUser();
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
    let newHolding = {
        email: "gregoryamitten@gmail.com",
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
            $rootScope.generateGraph();
            $uibModalStack.dismissAll();
        });
    }
}]);