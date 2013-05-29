var uploadReceipts = function(scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   //var createStatement = "CREATE TABLE IF NOT EXISTS Receipt (id INTEGER PRIMARY KEY, receipt TEXT, sync INTEGER)";
   var selectAllStatement = "SELECT receipt FROM Receipt WHERE sync=0";
   try
   {
      db.transaction(function(tx)
      {
         //
         // Retrieve Receipts
         //
         tx.executeSql(selectAllStatement, [], function(tx, result)
         {
            var items = [], item;
            var dataset = result.rows;
            for ( j = 0; j < dataset.length; j++)
            {
               item = dataset.item(j);
               items.push(item);
               //console.debug("Receipt TnId[" + item['tnId'] + "]");
            }

            scope.postMessage(JSON.stringify(
            {
               cmd : 'uploadReceipts',
               result : items
            }));
         }, function(tx, error)
         {
            console.debug("No Receipt Table found in SQL Database : " + error.message);
         });
      });
   }
   catch(e)
   {
   }
};
var insertReceipts = function(receipts, scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   try
   {
      db.transaction(function(tx)
      {
         for (var x = 0; x < receipts.length; x++)
         {
            var receipt = Ext.encode(receipts[x]);
            tx.executeSql(insertStatement, [receipt['id'], receipt], function()
            {
               scope.postMessage(JSON.stringify(
               {
                  cmd : 'insertReceipts',
                  result : receipts.length
               }));
            }, function(tx, error)
            {
               //console.debug("Failed to insert Customer(" + receipt.getId() + ") to Database : " +
               // error.message);
            });
         }
      });
   }
   catch(e)
   {
   }
};
var updateReceipts = function(ids, scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   var updateStatement = "UPDATE Receipt SET sync=1 WHERE id in (" + ids.toString() + ");";
   try
   {
      db.transaction(function(tx)
      {
         //
         // Update Receipt Database
         //
         tx.executeSql(updateStatement, [], function()
         {
            scope.postMessage(JSON.stringify(
            {
               cmd : 'updateReceipts',
               result : ids.length
            }));
         }, function(tx, error)
         {
         });
      });
   }
   catch(e)
   {
   }
};
var restoreReceipts = function(scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   var selectAllStatement = "SELECT receipt FROM Receipt";
   try
   {
      db.transaction(function(tx)
      {
         //
         // Retrieve Receipts
         //
         tx.executeSql(selectAllStatement, [], function(tx, result)
         {
            var items = [];
            var dataset = result.rows;
            for (var j = 0, item = null; j < dataset.length; j++)
            {
               item = dataset.item(j);
               items.push(item);
            }
            scope.postMessage(JSON.stringify(
            {
               cmd : 'restoreReceipts',
               result : items
            }));
         }, function(tx, error)
         {
         });
      });
   }
   catch(e)
   {
   }
};

if ( typeof (Worker) != 'undefined')
{
   onmessage = function(e)
   {
      var data = e.data;
      switch (data.cmd)
      {
         case 'uploadReceipts' :
         {
            uploadReceipts(self);
            break;
         }
         case 'insertReceipts' :
         {
            insertReceipt(data.receipts, self)
            break;
         }
         case 'updateReceipts' :
         {
            updateReceipts(data.ids, self);
            break;
         }
         case 'restoreReceipts' :
         {
            restoreReceipts(self);
            break;
         }
      }
   };
}
else
{
   Ext.define('Worker',
   {
      constructor : function(config)
      {
         var me = this;
         me.responseHandler =
         {
            postMessage : function(result)
            {
               me.onmessage(
               {
                  data : result
               });
            }
         }
      },
      postMessage : function(data)
      {
         switch (data.cmd)
         {
            case 'uploadReceipts' :
            {
               uploadReceipts(this.responseHandler);
               break;
            }
            case 'updateReceipts' :
            {
               updateReceipts(data.ids, this.responseHandler);
               break;
            }
            case 'restoreReceipts' :
            {
               restoreReceipts(this.responseHandler);
               break;
            }
         }
      },
      onmessage : Ext.emptyFn
   });
}
