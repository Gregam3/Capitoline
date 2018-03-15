var app = angular.module("capitoline", ['ui.bootstrap', 'smart-table', 'n3-line-chart', 'n3-pie-chart']);
angular.module("capitoline").factory('user', function ($http) {
    return null;
});

app.value('Email', 'gregoryamitten@gmail.com');
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

app.controller("overview", ['$scope', '$http', '$uibModal', '$rootScope', function ($scope, $http, $uibModal, $rootScope) {
    $rootScope.totalValue = 0.0;
    $rootScope.acquisitionCost = 0.0;
    $rootScope.user = null;
    $rootScope.profitting = null;
}]);


app.controller("totalValueLineChartCtrl", ['$scope', '$http', '$rootScope', function ($scope, $http, $rootScope) {
    $rootScope.historicalPortfolio = {
        total: [],
        crypto: [],
        stock: [],
        fiat: []
    };

    $scope.lineOptions = {
        series: [
            {
                axis: "y",
                dataset: "total",
                key: "value",
                label: "Total Value ($)",
                color: "#E7972D",
                type: ['line', 'area'],
                id: 'mySeries0'
            },
            {
                axis: "y",
                dataset: "crypto",
                key: "value",
                label: "Crypto Value ($)",
                color: "#139000",
                type: ['line', 'area'],
                id: 'mySeries1'
            },
            {
                axis: "y",
                dataset: "stock",
                key: "value",
                label: "Stock Value ($)",
                color: "#0000d6",
                type: ['line', 'area'],
                id: 'mySeries2'
            },
            {
                axis: "y",
                dataset: "fiat",
                key: "value",
                label: "Fiat Value ($)",
                color: "#ad0e00",
                type: ['line', 'area'],
                id: 'mySeries3'
            }

        ],
        axes: {
            x: {
                key: "time",
                type: 'date',
                tickFormat: d3.time.format("%b %y")
            },
            y: {}
        }
    };

    //Fetch and process graph data
    $rootScope.generateGraph = function () {
        $http.get(
            'http://localhost:8080/user/get/holding-graph-data/gregoryamitten@gmail.com'
        ).then(function (response) {
            $rootScope.historicalPortfolio.total = response.data.total;
            $rootScope.historicalPortfolio.crypto = response.data.crypto;
            $rootScope.historicalPortfolio.fiat = response.data.fiat;
            $rootScope.historicalPortfolio.stock = response.data.stock;
        });

        console.log($rootScope.historicalPortfolio);
    }
}]);

app.controller("diversificationPieChartCtrl", ['$scope', '$http', '$rootScope', function ($scope, $http, $rootScope) {
    $scope.pieOptions = {thickness: 10};
    $rootScope.portfolioDiversification = [];
}]);

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey) {
    $scope.user = null;


    $rootScope.updateUser = function () {

        $scope.cryptoValue = 0;
        $scope.stockValue = 0;
        $scope.fiatValue = 0;

        $rootScope.totalValue = 0;

        console.log("updating user");
        $rootScope.portfolioDiversification = [];
        $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
            .then(function (response) {
                $rootScope.user = response.data;
                $rootScope.acquisitionCost = 0;
                console.log(response.data);

                $rootScope.holdings = {
                    cryptos: {},
                    stocks: {},
                    fiats: {}
                };

                for (let i = 0; i < $rootScope.user.holdings.length; i++) {
                    const currentHolding = $rootScope.user.holdings[i];

                    if (currentHolding.holdingType === "STOCK") {
                        $rootScope.holdings.stocks[currentHolding.acronym] = currentHolding;
                    } else if (currentHolding.holdingType === "CRYPTO") {
                        $rootScope.holdings.cryptos[currentHolding.acronym] = currentHolding;
                    } else if (currentHolding.holdingType === "FIAT") {
                        $rootScope.holdings.fiats[currentHolding.acronym] = currentHolding;
                    }

                    for (let i = 0; i < currentHolding.transactions.length; i++) {
                        if(currentHolding.transactions[i].quantity > 0) {
                            $rootScope.acquisitionCost +=
                                currentHolding.transactions[i].price * currentHolding.transactions[i].quantity;
                        }
                    }
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
                        const currentValue = $rootScope.holdings.cryptos[holding].price * $rootScope.holdings.cryptos[holding].totalQuantity;

                        $rootScope.totalValue += currentValue;
                        $scope.cryptoValue += currentValue;

                    }

                    for (holding in $rootScope.holdings.fiats) {
                        $rootScope.holdings.fiats[holding].price =
                            (response.data[holding]) ? response.data[holding]["USD"] : null;

                        const currentValue = $rootScope.holdings.fiats[holding].price * $rootScope.holdings.fiats[holding].totalQuantity;

                        $rootScope.totalValue += currentValue;
                        $scope.fiatValue += currentValue;
                    }
                });

                //Get all User's stock prices
                $http.get("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=" + $scope.convertHoldingsToUriVariables($scope.holdings.stocks)
                    + "&apikey=" + AlphaVantageKey).then(function (response) {

                    let i = 0;

                    for (const holding in $rootScope.holdings.stocks) {
                        $rootScope.holdings.stocks[holding].price =
                            (response.data["Stock Quotes"][i]["2. price"]) ?
                                response.data["Stock Quotes"][i]["2. price"] : null;
                        const currentValue = $rootScope.holdings.stocks[holding].price * $rootScope.holdings.stocks[holding].totalQuantity;

                        $rootScope.totalValue += currentValue;
                        $scope.stockValue += currentValue;

                        i++;
                    }
                    $rootScope.generateGraph();
                    $rootScope.profitting = $rootScope.totalValue > $rootScope.acquisitionCost;

                    $rootScope.portfolioDiversification.push(
                        {
                            label: "Cryptos" + " - " + (($scope.cryptoValue / $scope.totalValue) * 100).toFixed(2) + "%",
                            value: $scope.cryptoValue,
                            color: '#139000'
                        },
                        {
                            label: "Fiat" + " - " + (($scope.fiatValue / $scope.totalValue) * 100).toFixed(2) + "%",
                            value: $scope.fiatValue,
                            color: '#ad0e00'
                        },
                        {
                            label: "Stocks" + " - " + (($scope.stockValue / $scope.totalValue) * 100).toFixed(2) + "%",
                            value: $scope.stockValue,
                            color: '#0000d6'
                        }
                    );
                });


            }
        );
        // $scope.$apply();
    };

    $rootScope.updateUser();

    $scope.convertHoldingsToUriVariables = function (currentHoldingsList) {
        let tickers = "";

        for (const holding in currentHoldingsList) {
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
            $rootScope.updateUser();
            $uibModalStack.dismissAll();
        });
    }
}]);