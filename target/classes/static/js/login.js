// var app = angular.module("login", ['ui.bootstrap', 'toaster']);
//
// app.controller("loginCtrl", ['$scope', '$http', 'toaster', '$window',
//     function ($scope, $http, toaster, $window) {
//         $scope.loginDetails = {
//             email: null,
//             password: null
//         };
//         $scope.registerDetails = {
//             email: null,
//             name: null,
//             password: null,
//             confirmedPassword: null
//         };
//
//         $scope.heading = "Log In";
//
//         $scope.login = function () {
//             $http({
//                 method: 'POST',
//                 url: "http://localhost:8080/security/login/",
//                 data: $scope.loginDetails
//             }).then(function successCallback(response) {
//                 toaster.pop('success', "Logged in", response.data);
//                 $window.location.reload();
//             }, function errorCallback(response) {
//                 console.log(response);
//                 toaster.pop('error', "Failed to Login", response.data);
//             });
//         };
//
//         $scope.register = function () {
//             if ($scope.registerDetails.password !== $scope.registerDetails.confirmedPassword) {
//                 toaster.pop('error', "Failed to Register", "Password's did not match");
//             } else {
//                 $http({
//                     method: 'POST',
//                     url: "http://localhost:8080/security/register/",
//                     data: $scope.registerDetails
//                 }).then(function successCallback(response) {
//                     toaster.pop('success', "You are now Registered", response.data);
//                     $window.location.reload();
//                 }, function errorCallback(response) {
//                     console.log(response);
//                     toaster.pop('error', "Failed to Register", response.data);
//                 });
//             }
//         };
//
//         $scope.changeHeading = function (newHeading) {
//             console.log(newHeading);
//             $scope.heading = newHeading;
//         }
//     }]);