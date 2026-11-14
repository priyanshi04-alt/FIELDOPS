"""
FieldOps Phase 4 — Predictive Inventory Replenishment Model Training
====================================================================
Compares:
1. Baseline: 7-Day Moving Average
2. ML Model A: Ridge Regression (L2 Linear)
3. ML Model B: Random Forest Regressor

Evaluates:
- Mean Absolute Error (MAE)
- Root Mean Squared Error (RMSE)
- R-squared (R2)

Target:
- Next 7-Day Aggregated Demand per Part

Exports:
- Model metadata, coefficients, and feature normalizers to backend JSON:
  backend/src/main/resources/models/inventory_forecast_model.json
"""

import json
import os
import numpy as np
import pandas as pd
from sklearn.linear_model import Ridge
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split

def train_inventory_models():
    csv_path = os.path.join("data", "simulation", "synthetic_inventory_consumption.csv")
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"Missing {csv_path}. Run generate_datasets.py first.")

    df = pd.read_csv(csv_path)
    df["date"] = pd.to_datetime(df["date"])

    # Aggregate daily consumption per part across regions
    daily_part = df.groupby(["part_id", "date"])["quantity_used"].sum().reset_index()

    # Create complete date range per part to avoid missing zero-consumption days
    all_parts = daily_part["part_id"].unique()
    all_dates = pd.date_range(daily_part["date"].min(), daily_part["date"].max(), freq="D")
    full_index = pd.MultiIndex.from_product([all_parts, all_dates], names=["part_id", "date"]).to_frame().reset_index(drop=True)

    merged = pd.merge(full_index, daily_part, on=["part_id", "date"], how="left").fillna({"quantity_used": 0})
    merged = merged.sort_values(["part_id", "date"]).reset_index(drop=True)

    # Feature Engineering (Chronological & Leak-Free)
    # Features derived strictly from past t: 7-day rolling sum, 30-day rolling sum, 7-day rolling std, 14-day rolling sum
    features_list = []
    
    for pid, group in merged.groupby("part_id"):
        group = group.copy()
        group["rolling_7d_consumption"] = group["quantity_used"].shift(1).rolling(7, min_periods=7).sum()
        group["rolling_14d_consumption"] = group["quantity_used"].shift(1).rolling(14, min_periods=14).sum()
        group["rolling_30d_consumption"] = group["quantity_used"].shift(1).rolling(30, min_periods=30).sum()
        group["rolling_7d_std"] = group["quantity_used"].shift(1).rolling(7, min_periods=7).std().fillna(0)
        
        # Target: Forward 7-day sum consumption [t, t+6]
        # Using negative shift or forward rolling window
        group["target_7d_demand"] = group["quantity_used"].iloc[::-1].rolling(7, min_periods=7).sum().iloc[::-1]

        features_list.append(group)

    dataset = pd.concat(features_list).dropna().reset_index(drop=True)
    
    feature_cols = [
        "rolling_7d_consumption",
        "rolling_14d_consumption",
        "rolling_30d_consumption",
        "rolling_7d_std"
    ]

    # Chronological Split (Train: First 80% of dates, Test: Last 20% of dates)
    unique_dates = np.sort(dataset["date"].unique())
    split_date = unique_dates[int(len(unique_dates) * 0.8)]

    train_data = dataset[dataset["date"] <= split_date]
    test_data = dataset[dataset["date"] > split_date]

    X_train = train_data[feature_cols]
    y_train = train_data["target_7d_demand"]
    X_test = test_data[feature_cols]
    y_test = test_data["target_7d_demand"]

    print(f"Inventory Forecast Dataset: Train samples = {len(X_train)}, Test samples = {len(X_test)}")

    # 1. Baseline: 7-Day Rolling Moving Average (Simply predicts that next 7 days equals past 7 days)
    y_pred_baseline = X_test["rolling_7d_consumption"]
    mae_base = mean_absolute_error(y_test, y_pred_baseline)
    rmse_base = np.sqrt(mean_squared_error(y_test, y_pred_baseline))
    r2_base = r2_score(y_test, y_pred_baseline)

    # 2. Ridge Regression
    ridge = Ridge(alpha=1.0)
    ridge.fit(X_train, y_train)
    y_pred_ridge = ridge.predict(X_test)
    mae_ridge = mean_absolute_error(y_test, y_pred_ridge)
    rmse_ridge = np.sqrt(mean_squared_error(y_test, y_pred_ridge))
    r2_ridge = r2_score(y_test, y_pred_ridge)

    # 3. Random Forest Regressor
    rf = RandomForestRegressor(n_estimators=50, max_depth=6, random_state=42)
    rf.fit(X_train, y_train)
    y_pred_rf = rf.predict(X_test)
    mae_rf = mean_absolute_error(y_test, y_pred_rf)
    rmse_rf = np.sqrt(mean_squared_error(y_test, y_pred_rf))
    r2_rf = r2_score(y_test, y_pred_rf)

    print("\n--- MODEL COMPARISON RESULTS ---")
    print(f"Baseline (Moving Avg)  : MAE = {mae_base:.3f}, RMSE = {rmse_base:.3f}, R2 = {r2_base:.3f}")
    print(f"Ridge Regression (ML)  : MAE = {mae_ridge:.3f}, RMSE = {rmse_ridge:.3f}, R2 = {r2_ridge:.3f}")
    print(f"Random Forest (ML)     : MAE = {mae_rf:.3f}, RMSE = {rmse_rf:.3f}, R2 = {r2_rf:.3f}")

    # Export Model Artifact for Spring Boot Java Inference
    # Ridge regression provides excellent explainability with direct linear coefficients
    model_payload = {
        "modelName": "INVENTORY_REPLENISHMENT_FORECAST",
        "modelVersion": "1.0.0",
        "algorithm": "Ridge Regression (L2 Regularized)",
        "evaluationDataset": "Synthetic Industrial Maintenance Logs (365 days)",
        "syntheticDisclaimer": "DEMO / SIMULATION DATA: Synthetic demonstration dataset; results do not represent production performance.",
        "metrics": {
            "baselineMae": round(float(mae_base), 3),
            "baselineRmse": round(float(rmse_base), 3),
            "modelMae": round(float(mae_ridge), 3),
            "modelRmse": round(float(rmse_ridge), 3),
            "modelR2": round(float(r2_ridge), 3),
            "rfMae": round(float(mae_rf), 3)
        },
        "intercept": float(ridge.intercept_),
        "coefficients": {
            "rolling_7d_consumption": float(ridge.coef_[0]),
            "rolling_14d_consumption": float(ridge.coef_[1]),
            "rolling_30d_consumption": float(ridge.coef_[2]),
            "rolling_7d_std": float(ridge.coef_[3])
        },
        "trainedAt": pd.Timestamp.now().isoformat(),
        "features": feature_cols
    }

    out_file = os.path.join("backend", "src", "main", "resources", "models", "inventory_forecast_model.json")
    with open(out_file, "w") as f:
        json.dump(model_payload, f, indent=2)
    print(f"\nModel exported successfully to: {out_file}")

if __name__ == "__main__":
    train_inventory_models()
