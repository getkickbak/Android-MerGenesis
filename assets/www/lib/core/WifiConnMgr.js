(function(cordova)
{
   window.plugins = window.plugins ||
   {
   };
   window.plugins.WifiConnMgr =
   {
      onPosConnected : function()
      {
         console.debug("WifiConnMgr::onPosConnected");
         //
         // Remove staled connection, and reconnect
         //
         if (pos && pos.wssocket)
         {
            console.debug("Reconnect with new POS Connection ...");
            pos.disconnect(true);
         }
      },
      establishPosConn : function()
      {
         cordova.exec(function()
         {
            console.log("WifiConnMgr::establishPosConn - SUCCESS");
         }, function(reason)
         {
            console.log("WifiConnMgr::establishPosConn - FAILED(" + reason + ")");
         }, "WifiConnMgr", "establishPosConn", []);
      },
      setIsPosEnabled : function(posEnabled)
      {
         cordova.exec(Ext.emptyFn, Ext.emptyFn, "WifiConnMgr", "setIsPosEnabled", [posEnabled]);
      }
   };

   cordova.addConstructor(function()
   {
      cordova.exec(function()
      {
         console.log("Initialized WifiConnMgr");
      }, function(reason)
      {
         console.log("Failed to initialize WifiConnMgr " + reason);
      }, "WifiConnMgr", "init", []);

   });
})(window.cordova || window.Cordova || window.PhoneGap);
