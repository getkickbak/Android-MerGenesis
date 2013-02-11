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
   //var callback = function(rc)
   {
      // rc could be registrationId or errorCode

      // CHANGE projectid & appid
      pushNotification.registerDevice(
      {
         projectid : pushNotifProjectId,
         appid : pushNotifAppId
      }, function(status)
      {
         var deviceToken = status, viewport;
         console.debug('registerDevice: ' + deviceToken);
         Genesis.constants.device =
         {
            'device_type' : pushNotifType, //1 for iOS, 3 for Android
            'device_id' : deviceToken
         };

         if (_application && (( viewport = _application.getController('client' + '.Viewport')) != null))
         {
            viewport.fireEvent('updateDeviceToken');
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
            var notification = event.notification, title = notification.title, userData = notification.userdata, viewport = _application.getController('client' + '.Viewport');

            console.debug('push notifcation - [' + JSON.stringify(notification) + ']');
            //if ( typeof (userData) != "undefined")
            {
               Ext.device.Notification.show(
               {
                  title : 'KICKBAK Notification',
                  message : title,
                  buttons : ['View Details', 'Cancel'],
                  callback : function(btn)
                  {
                     if (btn.toLowerCase() == 'view details')
                     {
                        viewport.setApsPayload(userData)
                        viewport.getGeoLocation();
                     }
                  }
               });
            }
         }
         else
         {
            console.warn('push notifcation - Null Notification');
         }
      });
   };

   //pushNotification.unregisterDevice(callback, callback);
}