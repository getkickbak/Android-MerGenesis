var createReceipts = function(scope)
{
   var createStatement = "CREATE TABLE IF NOT EXISTS Receipt (id INTEGER PRIMARY KEY, receipt TEXT, sync INTEGER)";
   var countStatement = "SELECT COUNT(id) AS cnt FROM Receipt";
   var countSyncStatement = "SELECT COUNT(id) AS cnt FROM Receipt WHERE sync=1";
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   try
   {
      db.transaction(function(tx)
      {
         //
         // Create Table
         //
         tx.executeSql(createStatement, [], function(tx, result)
         {
            scope.postMessage(JSON.stringify(
            {
               cmd : 'createReceipts'
            }));
         }, function(tx, error)
         {
            console.debug("Failed to create KickBak-Receipt Table : " + error.message);
         });
         //
         // Diagnostic Table
         //
         tx.executeSql(countStatement, [], function(tx, result)
         {
            console.debug(result.rows.item(0).cnt + " (TOTAL) EarnedReceipts in KickBak-Receipt DB");
         }, function(tx, error)
         {
         });
         //
         // Count Sync Entries
         //
         tx.executeSql(countSyncStatement, [], function(tx, result)
         {
            console.debug(result.rows.item(0).cnt + " (SYNC) EarnedReceipts in KickBak-Receipt DB");
         }, function(tx, error)
         {
         });
      });
   }
   catch(e)
   {
   }
}
var uploadReceipts = function(lastReceiptTime, scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   //var createStatement = "CREATE TABLE IF NOT EXISTS Receipt (id INTEGER PRIMARY KEY, receipt TEXT, sync INTEGER)";
   var selectAllStatement = "SELECT receipt FROM Receipt WHERE sync=0";
   var deleteStatement = "DELETE FROM Receipt WHERE id<? AND sync=1";
   try
   {
      db.transaction(function(tx)
      {
         if (lastReceiptTime > 0)
         {
            //console.debug("Removing all EarnedReceipts with TimeStamp : " + lastReceiptTime);
            tx.executeSql(deleteStatement, [lastReceiptTime], function(tx, result)
            {
               console.debug("All (SYNC) EarnedReceipts removed prior to " + Genesis.fn.convertDateFullTime(new Date(lastReceiptTime * 1000)));
            }, function(tx, error)
            {
               console.debug("No Receipt Table found in SQL Database : " + error.message);
            });
         }

         //
         // Retrieve Receipts to Upload
         //
         tx.executeSql(selectAllStatement, [], function(tx, res)
         {
            var items = [];
            var dataset = res.rows;
            for (var j = 0; j < dataset.length; j++)
            {
               //console.debug("uploadReceipts  --- item[" + j + "]=" + eval("[" + dataset.item(j) + "]"));
               items.push(dataset.item(j));
            }

            var output = JSON.stringify(
            {
               cmd : 'uploadReceipts',
               result : items
            });
            //console.debug("Receipts " + output);
            scope.postMessage(output);
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
   var insertStatement = "INSERT INTO Receipt (id, receipt, sync) VALUES (?, ?, ?)";
   try
   {
      db.transaction(function(tx)
      {
         for (var x = 0; x < receipts.length; x++)
         {
            var receipt = receipts[x];
            var id = receipt['id'];
            receipt = JSON.stringify(receipt);
            console.debug("Inserting Receipt ID[" + id + "] Content[" + receipt + "]");
            tx.executeSql(insertStatement, [id, receipt, 0], function(tx, result)
            {
               var rc = JSON.stringify(
               {
                  cmd : 'insertReceipts',
                  result : receipts.length
               });
               console.debug("Inserted Receipt into KickBak-Receipt Database : " + rc);
               scope.postMessage(rc);
            }, function(tx, error)
            {
               console.debug("Failed to insert Receipt(" + id + ") to Database : " + error.message);
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
   var updateStatement = "UPDATE Receipt SET sync=1 WHERE id IN (" + ids.toString() + ")";
   //console.debug("UPDATE Receipt SET sync=1 WHERE id IN (" + ids.toString() + ")")s;
   try
   {
      db.transaction(function(tx)
      {
         //
         // Update Receipt Database
         //
         tx.executeSql(updateStatement, [], function(tx, result)
         {
            //var dataset = result.rows;
            //if (dataset.length > 0)
            {
               scope.postMessage(JSON.stringify(
               {
                  cmd : 'updateReceipts',
                  result : ids.length
               }));
            }
            /*
             else
             {
             console.debug("updateReceipts --- No Receipt Entries were updated to SYNC");
             }
             */
         }, function(tx, error)
         {
            console.debug("updateReceipts --- No Receipt Entries were updated to SYNC");
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
var resetReceipts = function(scope)
{
   var db = openDatabase('KickBak', 'ReceiptStore', "1.0", 5 * 1024 * 1024);
   var dropStatement = "DROP TABLE Receipt";
   try
   {
      db.transaction(function(tx)
      {
         //
         // Drop Table
         //
         tx.executeSql(dropStatement, [], function(tx, result)
         {
            scope.postMessage(JSON.stringify(
            {
               cmd : 'resetReceipts'
            }));
         }, function(tx, error)
         {
            console.debug("resetReceipts --- Failed to drop KickBak-Receipt Table : " + error.message);
            scope.postMessage(JSON.stringify(
            {
               cmd : 'resetReceipts'
            }));
         });
      });
   }
   catch(e)
   {
   }
};

onmessage = function(e)
{
   var data = e.data;
   switch (data.cmd)
   {
      case 'createReceipts' :
      {
         createReceipts(self);
         break;
      }
      case 'uploadReceipts' :
      {
         uploadReceipts(data.lastReceiptTime, self);
         break;
      }
      case 'insertReceipts' :
      {
         insertReceipts(data.receipts, self)
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
      case 'resetReceipts':
      {
         resetReceipts(self);
         break;
      }
   }
};

//
// WebWorker not supported
//
if (( typeof (Worker) == 'undefined') && ( typeof (Ext) != 'undefined'))
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
            case 'createReceipts' :
            {
               createReceipts(this.responseHandler);
               break;
            }
            case 'uploadReceipts' :
            {
               uploadReceipts(data.lastReceiptTime, this.responseHandler);
               break;
            }
            case 'insertReceipts' :
            {
               insertReceipts(data.receipts, this.responseHandler);
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
            case 'resetReceipts':
            {
               resetReceipts(this.responseHandler);
               break;
            }
         }
      },
      onmessage : Ext.emptyFn
   });
}
else
{
   var Genesis =
   {
      fn :
      {
         convertDateFullTime : function(v)
         {
            return v.format('D, M d, Y \\a\\t g:i A');
         },
         currentDateTime : function(currentDate)
         {
            //return (this.systemTime - this.clientTime) + currentDate;
            return currentDate;
         },
         convertDateCommon : function(v, dateFormat, noConvert)
         {
            var date;
            var format = dateFormat || this.dateFormat;

            if (!( v instanceof Date))
            {
               if ( typeof (JSON) != 'undefined')
               {
                  //v = (jQuery.browser.msie) ? v.split(/Z$/)[0] : v.split('.')[0];
                  //v = (Ext.os.deviceType.toLowerCase() != 'desktop') ? v : v.split('.')[0];
                  //v = (Genesis.fn.isNative()) ? v : v.split('.')[0];
               }

               if (Ext.isEmpty(v))
               {
                  date = new Date();
               }
               else
               {
                  if (format)
                  {
                     date = Date.parse(v, format);
                     if (Ext.isEmpty(date))
                     {
                        date = new Date(v).format(format);
                     }
                     return [date, date];
                  }
                  date = new Date(v);
                  if (date.toString() == 'Invalid Date')
                  {
                     date = Date.parse(v, format);
                  }
               }
            }
            else
            {
               date = v;
            }
            if (!noConvert)
            {
               var currentDate = new Date().getTime();
               // Adjust for time drift between Client computer and Application Server
               var offsetTime = this.currentDateTime(currentDate);

               var timeExpiredSec = (offsetTime - date.getTime()) / 1000;

               if (timeExpiredSec > -10)
               {
                  if ((timeExpiredSec) < 2)
                     return [timeExpiredSec, 'a sec ago'];
                  if ((timeExpiredSec) < 60)
                     return [timeExpiredSec, parseInt(timeExpiredSec) + ' secs ago'];
                  timeExpiredSec = timeExpiredSec / 60;
                  if ((timeExpiredSec) < 2)
                     return [timeExpiredSec, 'a min ago'];
                  if ((timeExpiredSec) < 60)
                     return [timeExpiredSec, parseInt(timeExpiredSec) + ' mins ago'];
                  timeExpiredSec = timeExpiredSec / 60;
                  if ((timeExpiredSec) < 2)
                     return [date, '1 hr ago'];
                  if ((timeExpiredSec) < 24)
                     return [date, parseInt(timeExpiredSec) + ' hrs ago'];
                  timeExpiredSec = timeExpiredSec / 24;
                  if (((timeExpiredSec) < 2) && ((new Date().getDay() - date.getDay()) == 1))
                     return [date, 'Yesterday at ' + date.format('g:i A')];
                  if ((timeExpiredSec) < 7)
                     return [date, this.weekday[date.getDay()] + ' at ' + date.format('g:i A')];
                  timeExpiredSec = timeExpiredSec / 7;
                  if (((timeExpiredSec) < 2) && (timeExpiredSec % 7 == 0))
                     return [date, '1 wk ago'];
                  if (((timeExpiredSec) < 5) && (timeExpiredSec % 7 == 0))
                     return [date, parseInt(timeExpiredSec) + ' wks ago'];

                  if (timeExpiredSec < 5)
                     return [date, parseInt(timeExpiredSec * 7) + ' days ago']
                  return [date, null];
               }
               // Back to the Future! Client might have changed it's local clock
               else
               {
               }
            }

            return [date, -1];
         },
         convertDate : function(v, dateFormat)
         {
            var rc = this.convertDateCommon(v, dateFormat);
            if (rc[1] != -1)
            {
               return (rc[1] == null) ? rc[0].format('M d, Y') : rc[1];
            }
            else
            {
               return rc[0].format('D, M d, Y \\a\\t g:i A');
            }
         }
      }
   };
}

if ( typeof (importScripts) != 'undefined')
{
   importScripts('../../lib/core/date.js', '../../lib/core/extras.js', '../lib/core/date.js', '../lib/core/extras.js');
}
