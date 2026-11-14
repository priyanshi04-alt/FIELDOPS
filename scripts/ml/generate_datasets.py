"""
FieldOps Phase 4 — Synthetic Simulation Dataset Generator
=========================================================
Generates transparent, reproducible synthetic operational datasets for training
and evaluating Phase 4 predictive models:
1. Inventory Consumption Logs (365 days across 15 SKUs and 5 regions)
2. SLA Breach Historical Logs (1,500 historical work orders with operational features)

DISCLAIMER (SCIENTIFIC INTEGRITY):
This dataset is synthetic demonstration data designed for benchmarking and algorithm verification.
Results do not represent production performance from real-world industrial deployments.
"""

import json
import os
import random
from datetime import datetime, timedelta
import numpy as np
import pandas as pd

random.seed(42)
np.random.seed(42)

REGIONS = ["Delhi", "Gurgaon", "Noida", "Jaipur", "Chandigarh"]
PRIORITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
WORK_ORDER_TYPES = [
    "PREVENTIVE_MAINTENANCE",
    "EMERGENCY_REPAIR",
    "INSPECTION",
    "INSTALLATION",
    "CALIBRATION",
]

INVENTORY_ITEMS = [
    {"id": 61, "name": "Digital Multimeter Fluke 87V", "category": "ELECTRICAL", "base_stock": 25, "lead_days": 3},
    {"id": 62, "name": "Vibration Sensor SKF CMSS 2200", "category": "MECHANICAL", "base_stock": 18, "lead_days": 5},
    {"id": 63, "name": "Eaton 400V 630A MCCB", "category": "ELECTRICAL", "base_stock": 10, "lead_days": 7},
    {"id": 64, "name": "Siemens S7-1200 PLC Module", "category": "AUTOMATION", "base_stock": 8, "lead_days": 10},
    {"id": 65, "name": "Honeywell Temperature Transmitter", "category": "INSTRUMENTATION", "base_stock": 14, "lead_days": 4},
    {"id": 66, "name": "Pressure Sensor Danfoss MBS 3000", "category": "INSTRUMENTATION", "base_stock": 20, "lead_days": 3},
    {"id": 67, "name": "Schneider VFD Altivar 71", "category": "AUTOMATION", "base_stock": 6, "lead_days": 12},
    {"id": 68, "name": "Hydraulic Seal Kit Rexroth 50mm", "category": "HYDRAULIC", "base_stock": 30, "lead_days": 2},
    {"id": 69, "name": "Kromschroder Gas Solenoid Valve", "category": "HVAC", "base_stock": 12, "lead_days": 6},
    {"id": 70, "name": "Grundfos Mechanical Shaft Seal", "category": "MECHANICAL", "base_stock": 22, "lead_days": 4},
    {"id": 71, "name": "Flir E8-XT Infrared Camera", "category": "DIAGNOSTICS", "base_stock": 5, "lead_days": 14},
    {"id": 72, "name": "Fiber Optic Fusion Splicer Kit", "category": "TELECOM", "base_stock": 4, "lead_days": 15},
    {"id": 73, "name": "Pneumatic Cylinder Festo DNC 63", "category": "PNEUMATICS", "base_stock": 16, "lead_days": 5},
    {"id": 74, "name": "ABB Contactor AF52-30", "category": "ELECTRICAL", "base_stock": 28, "lead_days": 3},
    {"id": 75, "name": "Bearing Puller Hydraulic 10T", "category": "TOOLS", "base_stock": 7, "lead_days": 8},
]


def generate_inventory_consumption_data(days=365):
    """
    Simulates daily part consumption over 365 days.
    Includes seasonal trends, day-of-week patterns, and regional variance.
    """
    records = []
    start_date = datetime.now() - timedelta(days=days)

    for item in INVENTORY_ITEMS:
        item_id = item["id"]
        base_rate = max(0.5, item["base_stock"] / 10.0)

        for day_offset in range(days):
            current_date = start_date + timedelta(days=day_offset)
            dow = current_date.weekday()
            is_weekend = dow >= 5

            # Demand is ~40% lower on weekends for heavy industrial maintenance
            dow_multiplier = 0.6 if is_weekend else 1.1

            for region in REGIONS:
                region_mult = 1.3 if region in ["Delhi", "Gurgaon", "Noida"] else 0.85
                lambda_rate = base_rate * dow_multiplier * region_mult * 0.2

                # Poisson consumption
                quantity_used = np.random.poisson(lam=max(0.1, lambda_rate))

                if quantity_used > 0:
                    records.append({
                        "date": current_date.strftime("%Y-%m-%d"),
                        "day_of_week": dow,
                        "is_weekend": int(is_weekend),
                        "part_id": item_id,
                        "part_name": item["name"],
                        "category": item["category"],
                        "region": region,
                        "lead_days": item["lead_days"],
                        "quantity_used": int(quantity_used),
                    })

    df = pd.DataFrame(records)
    return df


def generate_sla_breach_data(sample_size=1600):
    """
    Simulates historical work orders with operational features and binary breach labels.
    Breach probability is deterministically driven by travel time, technician backlog,
    and remaining SLA window, with added operational variance.
    """
    records = []
    start_date = datetime.now() - timedelta(days=180)

    for i in range(sample_size):
        wo_id = 10000 + i
        created_at = start_date + timedelta(minutes=random.randint(0, 180 * 24 * 60))
        priority = random.choices(PRIORITIES, weights=[0.25, 0.40, 0.25, 0.10])[0]
        wo_type = random.choice(WORK_ORDER_TYPES)
        region = random.choice(REGIONS)

        # Operational features
        tech_backlog = random.randint(0, 5)
        distance_km = round(random.uniform(2.0, 48.0), 1)
        traffic_factor = random.uniform(1.0, 2.4)
        estimated_travel_minutes = int(distance_km * 2.2 * traffic_factor)
        service_duration_minutes = random.choice([30, 45, 60, 90, 120, 180])
        tech_historical_adherence = round(random.uniform(0.70, 0.99), 2)
        route_sequence_position = min(tech_backlog + 1, random.randint(1, 4))

        # Initial SLA window depending on priority
        sla_window_minutes = {
            "CRITICAL": 120,
            "HIGH": 240,
            "MEDIUM": 480,
            "LOW": 720,
        }[priority]

        remaining_sla_minutes = int(sla_window_minutes * random.uniform(0.10, 1.0))

        # Ground truth total time required before completion
        total_lead_time_required = (
            (route_sequence_position - 1) * 60  # Wait time for prior sequence jobs
            + estimated_travel_minutes
            + service_duration_minutes
            + int(np.random.normal(0, 15))  # Operational uncertainty
        )

        # Log-odds calculation for realistic class boundary
        # A breach occurs when required time exceeds remaining SLA window
        time_margin = remaining_sla_minutes - total_lead_time_required
        z = -0.04 * time_margin + 0.6 * tech_backlog - 2.5 * (tech_historical_adherence - 0.85)

        # Sigmoid probability
        prob_breach = 1.0 / (1.0 + np.exp(-np.clip(z, -6, 6)))
        breached = int(random.random() < prob_breach)

        records.append({
            "work_order_id": wo_id,
            "created_at": created_at.strftime("%Y-%m-%d %H:%M:%S"),
            "region": region,
            "priority": priority,
            "work_order_type": wo_type,
            "priority_weight": {"LOW": 1, "MEDIUM": 2, "HIGH": 3, "CRITICAL": 4}[priority],
            "remaining_sla_minutes": remaining_sla_minutes,
            "distance_km": distance_km,
            "estimated_travel_minutes": estimated_travel_minutes,
            "service_duration_minutes": service_duration_minutes,
            "technician_backlog": tech_backlog,
            "technician_historical_adherence": tech_historical_adherence,
            "route_sequence_position": route_sequence_position,
            "hour_of_day": created_at.hour,
            "day_of_week": created_at.weekday(),
            "breached": breached,
        })

    df = pd.DataFrame(records)
    return df


if __name__ == "__main__":
    output_dir = os.path.join("data", "simulation")
    os.makedirs(output_dir, exist_ok=True)

    print("Generating synthetic inventory consumption dataset...")
    df_inventory = generate_inventory_consumption_data(days=365)
    inv_path = os.path.join(output_dir, "synthetic_inventory_consumption.csv")
    df_inventory.to_csv(inv_path, index=False)
    print(f"Saved {len(df_inventory)} records to {inv_path}")

    print("Generating synthetic SLA breach telemetry dataset...")
    df_sla = generate_sla_breach_data(sample_size=1600)
    sla_path = os.path.join(output_dir, "synthetic_sla_breach_telemetry.csv")
    df_sla.to_csv(sla_path, index=False)
    print(f"Saved {len(df_sla)} records to {sla_path}")
    print(f"Class distribution: {df_sla['breached'].value_counts(normalize=True).to_dict()}")
