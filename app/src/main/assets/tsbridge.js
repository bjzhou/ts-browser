(function() {
    var screenOrientation = {};
    if (!window.OrientationType) {
        window.OrientationType = {
            'portrait-primary': 0,
            'portrait-secondary': 180,
            'landscape-primary': 90,
            'landscape-secondary': -90
        };
    }
    if (!window.OrientationLockType) {
        window.OrientationLockType = {
            'portrait-primary': 1,
            'portrait-secondary': 2,
            'landscape-primary': 4,
            'landscape-secondary': 8,
            portrait: 3, // either portrait-primary or portrait-secondary.
            landscape: 12, // either landscape-primary or landscape-secondary.
            any: 15 // All orientations are supported (unlocked orientation)
        };
    }
    screenOrientation.setOrientation = function (orientation) {
        TSBridge.requestScreenOrientation(orientation)
    };

    if (!screen.orientation) {
        screen.orientation = {};
    }

    setOrientationProperties();

    function addScreenOrientationApi (screenObject) {
        if (screenObject.unlock || screenObject.lock) {
            screenObject.nativeLock = screenObject.lock;
        }

        screenObject.lock = function (orientation) {
            var promiseLock;
            var p = new Promise(function (resolve, reject) {
                if (screenObject.nativeLock) {
                    promiseLock = screenObject.nativeLock(orientation);
                    promiseLock.then(
                        function success (_) {
                            resolve();
                        },
                        function error (_) {
                            screenObject.nativeLock = null;
                            resolveOrientation(orientation, resolve, reject);
                        }
                    );
                } else {
                    resolveOrientation(orientation, resolve, reject);
                }
            });
            return p;
        };
        screenObject.unlock = function () {
            screenOrientation.setOrientation('any');
        };
    }

    function resolveOrientation (orientation, resolve, reject) {
        screenOrientation.setOrientation(orientation);
        resolve('Orientation set'); // orientation change successful
    }

    addScreenOrientationApi(screen.orientation);

    var onChangeListener = null;

    Object.defineProperty(screen.orientation, 'onchange', {
        set: function (listener) {
            if (onChangeListener) {
                screen.orientation.removeEventListener('change', onChangeListener);
            }
            onChangeListener = listener;
            if (onChangeListener) {
                screen.orientation.addEventListener('change', onChangeListener);
            }
        },
        get: function () {
            return onChangeListener || null;
        },
        enumerable: true,
        configurable: true
    });

    var evtTarget = new XMLHttpRequest(); // document.createElement('div');
    var orientationchange = function () {
        setOrientationProperties();
        var event = document.createEvent('Events');
        event.initEvent('change', false, false);
        evtTarget.dispatchEvent(event);
    };

    screen.orientation.addEventListener = function (a, b, c) {
        return evtTarget.addEventListener(a, b, c);
    };

    screen.orientation.removeEventListener = function (a, b, c) {
        return evtTarget.removeEventListener(a, b, c);
    };

    function setOrientationProperties () {
        switch (window.orientation) {
        case 0:
            screen.orientation.type = 'portrait-primary';
            break;
        case 90:
            screen.orientation.type = 'landscape-primary';
            break;
        case 180:
            screen.orientation.type = 'portrait-secondary';
            break;
        case -90:
            screen.orientation.type = 'landscape-secondary';
            break;
        default:
            screen.orientation.type = 'portrait-primary';
            break;
        }
        screen.orientation.angle = window.orientation || 0;
    }
    window.addEventListener('orientationchange', orientationchange, true);
}());