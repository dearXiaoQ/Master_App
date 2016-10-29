/**
 * Created by Administrator on 16-3-25.
 */
var app = angular.module('index', ['ionic', 'httpModule']);
app.controller('foodMenuController', function ($scope, $ionicScrollDelegate, $state, $rootScope, dialogService, httpService, dataExChange) {

    var subItemSelectedCount = 0;

    var foodMenuScrollHandle = 'foodMenuScrollHandle';

    //重新计算foodMenuScroll 容器大小
    $scope.resizeScroll = function () {
        $ionicScrollDelegate.$getByHandle(foodMenuScrollHandle).resize();
    };
    //当前
    $scope.setHeight = {'height': (document.documentElement.clientHeight - 46) + "px"};


    $scope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
        //若在直接进入当前页面的则将其转向首页
        if (toState.name == 'index') {
            if (dataExChange.getNewOrderFlag() == true) {
                getFoodMenuItemFromService();
                dataExChange.setNewOrderOk(false);
            }

        }
        if (fromState.name == 'index') {
            dialogService.log('go out from  foodMenuController');
        }
    });
    //当前菜品列表
    //$scope.foodMenuItems = undefined;

    $scope.menuObj = {
        foodMenuItems: undefined,
        menuListSelectId: undefined
    };

    //点击菜品勾选事件
    $rootScope.hasOrder = false;
    $scope.onMenuCheckSelectClick = function (item) {
        //item.selected =!item.selected;
        dataExChange.setOrder($scope.menuObj.foodMenuItems);
        console.log('item :' + JSON.stringify(item));
        console.log('in onMenuCheckSelectClick selected = ' + item.selected);
        if (item.selected == true) {
            $rootScope.hasOrder = true;
            subItemSelectedCount++;
            //if (subItemSelectedCount > 3) {
            //    item.selected = false;
            //    subItemSelectedCount--;
            //    dialogService.showConfirm({
            //        showText: '你已点了3个菜品请下单'
            //    }, function (res) {
            //        if (res) {
            //            dataExChange.setOrder($scope.menuObj.foodMenuItems);
            //            $state.go('order');
            //            console.log("你按了确定键");
            //        } else {
            //            console.log("你按了取消键");
            //        }
            //    });
            //}
        } else {
            subItemSelectedCount--;
            if (subItemSelectedCount <= 0) {
                subItemSelectedCount = 0;
                $rootScope.hasOrder = false;
            }
        }
        console.log(' subItemSelectedCount  = ' + subItemSelectedCount);
    };

    var buildFoodMenuItems = function (menu) {
        //if($scope.foodMenuItems == undefined){
        //    for(var i in menu){
        //        $scope.foodMenuItems[i]={
        //            'foodId':menu[i].foodId,
        //            'name':menu[i].mane,
        //            'foodMenuArr':menu[i].typeId
        //        };
        //        for(var j in $scope.foodMenuItems[i].foodMenuArr){
        //            $scope.foodMenuItems[i].foodMenuArr[j].selected = false;
        //        }
        //    }
        //
        //}else{
        //    var foodMenuItemCount=0
        //}
        $scope.menuObj.foodMenuItems = menu;
        $scope.menuObj.menuListSelectId = menu[0].foodId;
        for (var i in $scope.menuObj.foodMenuItems) {
            $scope.menuObj.foodMenuItems[i].count = 0;
            for (var j in $scope.menuObj.foodMenuItems[i].typeId) {
                $scope.menuObj.foodMenuItems[i].count += $scope.menuObj.foodMenuItems[i].typeId[j].count;
            }
        }
        dataExChange.setOrder($scope.menuObj.foodMenuItems);
        subItemSelectedCount = 0;
        $rootScope.hasOrder = false;
    };

    //从服务器获取菜品列表
    var postGetMenu = function () {
        httpService.post('http://' + dataExChange.domain + '/mastercook/food.api.php', dataExChange.accessToken, function (data) {
            dialogService.log('post ok');
            dialogService.log('data : ' + JSON.stringify(data));
            if (data.errorCode == 0) {
                buildFoodMenuItems(data.result);
            } else {
                switch (data.errorCode) {
                    case 1000:
                        dialogService.showAlert('验证失败');
                        break;
                    default :
                        break;
                }
            }
            dialogService.loadingHideService();
            $scope.$broadcast('scroll.refreshComplete');
        }, function (data) {
            dialogService.log('post error');
            dialogService.log('data : ' + data);
            dialogService.loadingHideService();
            $scope.$broadcast('scroll.refreshComplete');
        });
    };
    var getFoodMenuItemFromService = function () {
        dialogService.loadingShowService();
        postGetMenu();
    };
    //foodMenuController初始化
    $scope.foodMenuControllerInit = function () {
        getFoodMenuItemFromService();
    };
    $scope.doRefresh = function () {
        postGetMenu();
    };

    $scope.menuListOnClick = function (menuList) {
        $scope.menuObj.menuListSelectId = menuList.foodId;
        //$scope.resizeScroll();
        var scroll = document.getElementById(menuList.foodId).offsetTop;
        $ionicScrollDelegate.resize();
        $ionicScrollDelegate.scrollTo(0, scroll, true);
    };
});

app.controller('hotPotController', function ($scope, $interval, $timeout, httpService, dataExChange, dialogService) {
    var setHello = 'hello word dcl!!';
    var intervalHandle = undefined;
    $scope.rangeBarWidth = document.getElementById('rangeBar').clientWidth - 30;
    $scope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
        //若在直接进入当前页面的则将其转向首页
        if (toState.name == 'dcl') {
            dialogService.log('in to  hotPotController');
            checkStatus();
            intervalHandle = $interval(checkStatus, 5000);
        }
        if (fromState.name == 'dcl') {
            dialogService.log('go out from  hotPotController');
            $interval.cancel(intervalHandle);
            intervalHandle = undefined;

        }
    });
    $scope.OnReleaseCallback = function () {
        dialogService.log('in OnReleaseCallback');
        $scope.autoCookObj.holdOn = false;
        $scope.autoCookObj.step = 0;
        $scope.powerRange.start = true;
        $scope.powerSWonClick($scope.powerRange);
    };


    $scope.setHeight = {'height': (document.documentElement.clientHeight - 46) + "px"};

    $scope.setHello = setHello;
    $scope.powerRange = {
        color: '#727272',
        process: 0,
        PI: Math.PI * 2 * 136,
        start: false,
        model: 1,
        autoStep: -1
    };

    var cmdOn = false;

    var checkStatus = function () {
        var powerObj = {
            'action': 'returnPower',
            'user': dataExChange.accessToken.user,
            'passWord': dataExChange.accessToken.passWord
        };
        httpService.post('http://' + dataExChange.domain + '/mastercook/ble.api.php', powerObj, function (data) {
            dialogService.log('post ok');
            dialogService.log('data : ' + JSON.stringify(data));
            if (data.errorCode == 0) {
                dialogService.log('data ok');
                if (cmdOn == false) {

                    if (data.result.powerSw) {
                        $scope.powerRange.start = data.result.powerSw;
                    } else {
                        $scope.powerRange.start = false
                    }
                    if ($scope.powerRange.start == false) {
                        $scope.powerRange.color = '#727272';
                        if ($scope.autoCookObj.holdOn == true) {
                            stopAutoCook();
                        }
                    } else {
                        if (data.result.power) {
                            $scope.powerRange.process = data.result.power / 200;
                            $scope.powerRange.color = '#21b4ff';
                        }
                    }
                    dialogService.log('powerRange : ' + JSON.stringify($scope.powerRange));
                }

            } else {
                dialogService.log('data error');
            }
            //dialogService.loadingHideService();
        });
    };


    $scope.autoCookObj = {
        holdOn: false,
        step: 0,
        powerArr: [{
            time: 60,//秒
            power: 2000
        }, {
            time: 60,//秒
            power: 1000
        }, {
            time: 60,//秒
            power: 200
        }, {
            time: 60,//秒
            power: 800
        }]
    };

    var stopAutoCook = function () {
        $scope.autoCookObj.step = 0;
        $scope.autoCookObj.holdOn = false;
        $scope.powerRange.process = 0;
        $scope.powerRange.start = false;
        $scope.powerSWonClick($scope.powerRange);
    };
    $scope.startAutoCookClick = function () {
        if ($scope.autoCookObj.holdOn == true) {
            stopAutoCook();
        } else {
            $scope.autoCookObj.step = 0;
            $scope.autoCookClick();
        }
    };
    $scope.autoCookClick = function () {
        dialogService.log('in autoCook');
        $scope.autoCookObj.holdOn = true;
        $scope.powerRange.autoStep = $scope.autoCookObj.step;
        $scope.powerRange.process = $scope.autoCookObj.powerArr[$scope.autoCookObj.step].power / 200;
        $scope.powerRange.start = true;
        $scope.powerSWonClick($scope.powerRange);
        $timeout(function () {
            $scope.autoCookObj.step++;
            if ($scope.autoCookObj.step <= 3) {
                if ($scope.autoCookObj.holdOn == true) {
                    $scope.autoCookClick();
                } else {
                    $scope.autoCookObj.step = 0;
                    $scope.autoCookObj.holdOn = false;
                }
            } else {
                stopAutoCook();
            }
        }, $scope.autoCookObj.powerArr[$scope.autoCookObj.step].time * 1000);
    };
    $scope.powerSWonClick = function (power) {
        cmdOn = true;
        dialogService.log('powerSWonClick in  power:' + JSON.stringify(power));
        dialogService.log('powerSWonClick in  powerRange:' + JSON.stringify($scope.powerRange));
        dialogService.loadingShowService();

        if (power.process == 3) {
            power.process = 2;
        }
        var powerTmp = power.process;
        if (power.process == 0) {

            //alert('process == 0');
            dialogService.log('process == 0');
            //power.process = 0;
            powerTmp = 1;
            power.start = false;
        }
        var powerObj = {
            'action': 'toCooker',
            'user': dataExChange.accessToken.user,
            'passWord': dataExChange.accessToken.passWord,
            'powerSw': power.start ? 1 : 0,
            'power': powerTmp * 200,
            'model': $scope.powerRange.model
        };

        httpService.post('http://' + dataExChange.domain + '/mastercook/ble.api.php', powerObj, function (data) {
            dialogService.log('post ok');
            dialogService.log('data : ' + JSON.stringify(data));
            if (data.errorCode == 0) {
                dialogService.log('data ok');
            } else {
                dialogService.log('data error');
            }
            cmdOn = false;
            dialogService.loadingHideService();
        });
    };
});

function isWeiXin() {
    var ua = window.navigator.userAgent.toLowerCase();
    if (ua.match(/MicroMessenger/i) == 'micromessenger') {
        return true;
    } else {
        return false;
    }
}
app.controller('fanController', function ($scope) {
    $scope.fanPlay = false;
});
app.controller('panController', function ($scope) {
    $scope.panTemp = 30;
});
app.controller('orderController', function ($scope, $rootScope, $interval, $ionicScrollDelegate, dataExChange, dialogService, httpService) {
    var order = '没有订单';
    $scope.isActive = false;


    if (isWeiXin() == false) {
        LampObject.controlLamp($scope.isActive?1:2);
    }


    $scope.lightClick = function () {
        $scope.isActive = !$scope.isActive;
        if (isWeiXin() == false) {
            LampObject.controlLamp($scope.isActive?1:2);
        }
    };
    $scope.order = {orderItem: [], orderHistoryList: []};
    var foodItem = dataExChange.getOrder();
    $scope.setHeight = {'height': (document.documentElement.clientHeight - 46) + "px"};
    //界面载入成功后调用
    var intervalHandle = undefined;
    $scope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
        stateFrom = fromState.name;
        //若在直接进入当前页面的则将其转向首页
        if (toState.name == 'order') {
            $scope.isCollapsed = false;
            $scope.orderControllerInit();
            initOk = true;

            //intervalHandle = $interval(getHistoryListFromService, 10000);
            dialogService.log('in to order controller');
        }
        if (fromState.name == 'order') {
            dialogService.log('go out from order controller');
            initOk = false;
            $interval.cancel(intervalHandle);
            intervalHandle = undefined;
        }
    });

    var initOk = false;

    $scope.resizeScroll = function () {
        $ionicScrollDelegate.$getByHandle('orderScrollHandle').resize();
    };

    var getHistoryListFromService = function () {
        return;
        httpService.post('http://' + dataExChange.domain + '/mastercook/order.api.php', {
            'user': dataExChange.accessToken.user,
            'passWord': dataExChange.accessToken.passWord,
            "action": 'check',  //订单动作  order:下单    check：查单     取消订单：cancel
            "username": dataExChange.userName
        }, function (data) {
            dialogService.loadingHideService();
            dialogService.log('getHistoryList: ' + JSON.stringify(data));
            if (data.errorCode == 0) {
                $scope.order.orderHistoryList = data.result;
                if ($scope.order.orderHistoryList.length > 0) {
                    dataExChange.hasOrderHistory = true;
                } else {
                    dataExChange.hasOrderHistory = false;
                }
            }
        }, function () {
            dialogService.loadingHideService();
            dialogService.log('服务器错误');
        });
    };
    $scope.orderControllerInit = function () {

        if (initOk == false) {

            getHistoryListFromService();
            dialogService.log('orderControllerInit start');
            foodItem = dataExChange.getOrder();
            $scope.order.orderItem = [];


            for (var i in foodItem) {
                for (var j = 0 in foodItem[i].typeId) {
                    if (foodItem[i].typeId[j].selected == true) {
                        $scope.order.orderItem.push(foodItem[i].typeId[j]);
                    }
                }
            }
        }
    };


    var orderOk = function () {
        $scope.order.orderItem = [];
        dataExChange.setOrder($scope.order.orderItem);
        $rootScope.hasOrder = false;
        dataExChange.setNewOrderOk(true);
        getHistoryListFromService();
        $scope.isCollapsed = true;

    };
    var orderToService = function () {
        dialogService.loadingShowService();
        var foodIdList = [];
        for (var i in $scope.order.orderItem) {
            foodIdList.push($scope.order.orderItem[i].foodId);
        }
        httpService.post('http://' + dataExChange.domain + '/mastercook/order.api.php', {
            'user': dataExChange.accessToken.user,
            'passWord': dataExChange.accessToken.passWord,
            "action": 'order',  //订单动作  order:下单    check：查单     取消订单：cancel
            "username": dataExChange.userName,
            "foodList": foodIdList
        }, function (data) {
            dialogService.loadingHideService();
            switch (data.errorCode) {
                case 0:
                    dialogService.showAlert('恭喜，下单成功请耐心等待配送', function () {

                        //dialogService.log('已扫码确认支付，支付码 = ' + res.resultStr);
                        orderOk();
                    });
                    break;
                case 2:
                    var foodNameStr = '';
                    for (var i in data.result) {
                        foodNameStr += data.result[i].name;
                        foodNameStr += '、';
                    }
                    foodNameStr += '已经没有了，请重新下单';
                    dialogService.showAlert(foodNameStr, function () {
                        //dialogService.log('已扫码确认支付，支付码 = ' + res.resultStr);
                    });
                    $scope.order.orderItem = [];
                    dataExChange.setOrder($scope.order.orderItem);
            }
        }, function () {
            dialogService.loadingHideService();
            dialogService.log('服务器错误');
        });
    };

    $scope.enSureOrder = function () {
        dialogService.showConfirm({
            showText: '订单应付 0 元，扫码确认支付？'
        }, function (res) {
            if (res) {
                console.log("你按了确定键");
                if (dataExChange.isInWeiXin() == true) {
                    wx.scanQRCode({
                        needResult: 1,
                        success: function (res) {
                            //sDialog.loadingShow();
                            if (res.resultStr) {
                                if (res.resultStr == 'http://mp.weixin.qq.com/mp/wifi?q=1ef525a4e27b493b') {
                                    orderToService();
                                } else {
                                    dialogService.showAlert('二维码不匹配，请重新下单');
                                }

                                //alert("获取SN码成功 :"+res.resultStr);
                            } else {
                                //alert("SN码异常");
                            }
                        }
                    });
                } else {
                    dialogService.showAlert('亲！人品爆发，不用钱', function () {
                        orderToService();
                    });
                }

            } else {
                console.log("你按了取消键");
            }
        });

        //var orderItemIdArr = [];
        //for(var i in $scope.order.orderItem){
        //    orderItemIdArr[i] = $scope.order.orderItem[i].foodId;
        //}
        //
        //dialogService.loadingShowService();
        //httpService.post('http://'+dataExChange.domain+'/mastercook/order.api.php',
        //    {
        //        'user':dataExChange.accessToken.user,
        //        'passWord':dataExChange.accessToken.passWord,
        //        "userId":123,
        //        "order":orderItemIdArr
        //    }, function (data) {
        //    dialogService.log('post ok');
        //    dialogService.log('data : ' + JSON.stringify(data));
        //    if (data.errorCode == 0) {
        //        $scope.foodMenuItems = data.result;
        //    } else {
        //        switch (data.errorCode) {
        //            case 1000:
        //                dialogService.showAlert('验证失败');
        //                break;
        //            default :
        //                break;
        //        }
        //    }
        //    dialogService.loadingHideService();
        //}, function (data) {
        //    dialogService.log('post error');
        //    dialogService.log('data : ' + data);
        //    dialogService.loadingHideService();
        //});
    }

});

app.controller('indexController', function ($scope, $state, $rootScope, httpService, dialogService, dataExChange) {

    //TODO:vijanny 2016-3-28：首页结构
    //菜单控制页OBJ
    var menuObj = (function () {
        var menuOpt = {
            foodMenu: {
                index: 'food',
                title: '菜品',
                id: 1
            },
            hotPotControl: {
                index: 'hotPot',
                title: '开涮',
                id: 2

            },
            order: {
                index: 'order',
                title: '订单',
                id: 3

            }
        };


        var changeMenu = function (menu) {
            //switch (menu) {
            //    case 'food':
            //        $scope.menuOpt = menuOpt.foodMenu;
            //        $state.go('index');
            //        break;
            //    case 'hotPot':
            //        if (dataExChange.hasOrderHistory == true) {
            //            $scope.menuOpt = menuOpt.hotPotControl;
            //            $state.go('dcl');
            //        }
            //        break;
            //    case 'order':
            //        $scope.menuOpt = menuOpt.order;
            //        $state.go('order');
            //        break;
            //    default :
            //        break;
            //}

        };

        return {
            changeMenu: changeMenu,
            menuOpt: menuOpt
        };

    })();

    var wxConfig = (function () {
        httpService.post('http://' + dataExChange.domain + '/mastercook/wx/wx.config.php', {}, function (data) {
            dialogService.log('post ok');
            dialogService.log('data : ' + JSON.stringify(data));

            var config = {
                debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: data.appId, // 必填，公众号的唯一标识
                timestamp: data.timestamp, // 必填，生成签名的时间戳
                nonceStr: data.nonceStr, // 必填，生成签名的随机串
                signature: data.signature,// 必填，签名，见附录1
                jsApiList: ['scanQRCode']
            };
            dialogService.log('data : ' + JSON.stringify(config));
            //alert(JSON.stringify(config));
            wx.config(config);
            wx.ready(function () {
                //alert('wx ready');
                dialogService.log('ready ok');
            });
        }, function (data) {
            dialogService.log('post error');
            dialogService.log('data : ' + data);
            dialogService.loadingHideService();
        });
    })();

    $scope.menuOptArr = [{
        info: menuObj.menuOpt.foodMenu
    }, {
        info: menuObj.menuOpt.hotPotControl
    }];


    $scope.menuChangeProcess = function (menu) {
        menuObj.changeMenu(menu);
    };

    $scope.indexControllerInit = function () {
        menuObj.changeMenu('food');
    };
    $rootScope.hasOrder = false;
});
app.service('dataExChange', function () {
    var self = this;

    //alert(window.location.host);

    var orderFoodItem;
    var newOrderOk = false;
    var userName = Date.parse(new Date());
    self.userName = userName.toString();
    var ua = navigator.userAgent.toLowerCase();
    var isWeixin = ua.indexOf('micromessenger') != -1;

    self.hasOrderHistory = false;


    self.setNewOrderOk = function (flag) {
        newOrderOk = flag;
    };
    self.getNewOrderFlag = function () {
        return newOrderOk;
    };
    self.accessToken = {
        user: 'masterSoftWaver',
        passWord: 'masterYJS'
    };
    //this.domain ='14q9127i71.imwork.net';
    self.domain = window.location.host;
    if (!isWeixin && self.domain == '14q9127i71.imwork.net') {
        document.head.innerHTML = '<title>抱歉，出错了</title><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=0"><link rel="stylesheet" type="text/css" href="https://res.wx.qq.com/connect/zh_CN/htmledition/style/wap_err1a9853.css">';
        document.body.innerHTML = '<div class="page_msg"><div class="inner"><span class="msg_icon_wrp"><i class="icon80_smile"></i></span><div class="msg_content">请在微信客户端打开链接 </div></div></div>';
    }
    self.isInWeiXin = function () {
        //return (self.domain == '14q9127i71.imwork.net');
        return (isWeixin && self.domain == '14q9127i71.imwork.net');
    };
    self.setOrder = function (order) {
        orderFoodItem = order;
    };
    self.getOrder = function () {
        return orderFoodItem;
    };

});
app.service('dialogService', function ($ionicPopup, $ionicLoading, $rootScope) {

    var self = this;
    var loadingShowEn = true;


    var debugFlag = true;

    this.log = function (str) {
        if (debugFlag == true) {
            console.log(str);
        }
    };

    this.loadingShowService = function () {
        console.log('加载中..');
        if (loadingShowEn == true) {
            $ionicLoading.show({
                template: '加载中...'
            });
            loadingShowEn = false;
        }

    };

    this.loadingHideService = function () {
        if (loadingShowEn == false) {
            $ionicLoading.hide();
            loadingShowEn = true;
        }

    };
    //确认询问对话框-----------------------------------------------------------------------
    this.showConfirm = function (confirmConfig, callBack) {
        if (confirmConfig.cancelText == undefined) {
            confirmConfig.cancelText = '取消';
        }
        if (confirmConfig.okText == undefined) {
            confirmConfig.okText = '确定';
        }
        if (confirmConfig.warnFlag == undefined) {
            $rootScope.iconWarn = false;
        } else {
            $rootScope.iconWarn = confirmConfig.warnFlag;
        }
        var confirmPopup = $ionicPopup.confirm({
            title: confirmConfig.showText,
            subTitle: '<p></p><p></p>',
            //templateUrl: 'partials/confirm.html',
            //cssClass: 'popupBody',
            cancelText: confirmConfig.cancelText, // String (default: 'Cancel'). The text of the Cancel button.
            //cancelType: 'button-full button-light', // String (default: 'button-default'). The type of the Cancel button.
            okText: confirmConfig.okText // String (default: 'OK'). The text of the OK button.
            //okType: 'button-full button-light' // String (default: 'button-positive'). The type of the OK button.
        });
        confirmPopup.then(function (res) {
            if (callBack) {
                callBack(res);
            }
        });
    };
    this.showAlert = function (msg, callBack) {

        var confirmPopup = $ionicPopup.alert({
            title: msg
        });
        confirmPopup.then(function (res) {
            if (callBack) {
                callBack(res);
            }
        });
    };

});

///*ui-router 路由配置*/
//app.config(function($stateProvider, $urlRouterProvider) {
//
//    $stateProvider
//        .state('index', {
//            url: "/index",
//            templateUrl: "partials/hotPot.html",
//            controller:'foodMenuController'
//        })
//        .state('dcl', {
//            url: "/dcl",
//            templateUrl: "partials/dcl.html"
//            //controller:'hotPotController'
//        }).state('order', {
//            url: "/order",
//            templateUrl: "partials/order.html"
//            //controller:'orderController'
//        });
//    $urlRouterProvider.otherwise("/order");
//});

