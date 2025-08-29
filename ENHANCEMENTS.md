
ENHANCEMENTS ADDED:
1) Dashboard endpoints:
   - POST /api/dashboard/model?model=lstm|arima   -> set default model
   - GET  /api/dashboard/model                    -> get default model
   - GET  /api/dashboard/alerts/run               -> run inventory checks and return alerts
   - POST /api/dashboard/reorder?productId=...&storeId=...&qty=...  -> create reorder
   - POST /api/dashboard/scenario/simulate       -> simulate scenario (JSON body)
2) AlertService: scheduled inventory checks and auto-reorder hooks.
3) ReorderService: creates a simple reorder object (prints to console).
4) ScenarioAnalysisService: simulate promotion/seasonality/external event impacts on forecast.
