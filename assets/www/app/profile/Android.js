Ext.define('Genesis.profile.Android',
{
   extend : 'Ext.app.Profile',
   config :
   {
   },
   isActive : function()
   {
      return Ext.os.is('Android');
   }
});

//---------------------------------------------------------------------------------------------------------------------------------
// Ext.device.notification.PhoneGap
//---------------------------------------------------------------------------------------------------------------------------------
Ext.define('Genesis.device.notification.PhoneGap',
{
   override : 'Ext.device.notification.PhoneGap',
   beep : function(times)
   {
      var viewport = _application.getController(((merchantMode) ? 'server' : 'client') + '.Viewport');
      viewport.self.playSoundFile(viewport.sound_files['beepSound']);
      //navigator.notification.beep(times);
   },
   vibrate : function(duration)
   {
      navigator.notification.vibrate(duration || 1000);
   },
   activityStart : function(title, message)
   {
      navigator.notification.activityStart(title, message);
   },
   activityStop : function()
   {
      navigator.notification.activityStop();
   }
});

//---------------------------------------------------------------------------------------------------------------------------------
// PushWoosh Push Notification API
//---------------------------------------------------------------------------------------------------------------------------------
function initPushwoosh()
{
   var pushNotification = window.plugins.pushNotification;
   var callback = function(rc)
   {
      // rc could be registrationId or errorCode

      // CHANGE projectid & appid
      pushNotification.registerDevice(
      {
         projectid : 733275653511,
         appid : pushNotifAppId
      }, function(status)
      {
         var deviceToken = status;
         console.debug('registerDevice: ' + deviceToken);
         Genesis.constants.device =
         {
            'device_type' : pushNotifType, //1 for iOS, 3 for Android
            'device_id' : deviceToken
         };

         var mainPage = _application.getController('client' + '.MainPage'), viewport = _application.getController('client' + '.Viewport');
         if (viewport.getLoggedIn() && !mainPage.updatedDeviceToken)
         {
            Account['setUpdateRegUserDeviceUrl']();
            console.log("setUpdateRegUserDeviceUrl - Refreshing Device Token ...");
            Account.getProxy().supressErrorsPopup = true;
            Account.load(0,
            {
               jsonData :
               {
               },
               params :
               {
                  device : Ext.encode(Genesis.constants.device)
               },
               callback : function(record, operation)
               {
                  Account.getProxy().supressErrorsPopup = false;
               }
            });
         }
      }, function(status)
      {
         console.debug('failed to register : ' + JSON.stringify(status));
         Genesis.constants.device = null;
      });

      document.addEventListener('push-notification', function(event)
      {
         if (event.notification)
         {
            var title = event.notification.title;
            var userData = event.notification.userdata;

            if ( typeof (userData) != "undefined")
            {
               Ext.device.Notification.show(
               {
                  title : 'Push Notification Alert',
                  message : title,
                  buttons : ['OK']
               });
               console.warn('push notifcation - userData [' + JSON.stringify(userData) + ']');
            }
         }
         else
         {
            console.warn('push notifcation - Null Notification');
         }
      });
   };

   pushNotification.unregisterDevice(callback, callback);
}