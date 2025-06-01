import pandas as pd
import matplotlib.pyplot as plt

# Read the CSV file
df = pd.read_csv('report.csv')

# Print column names to verify
print("Columns in the DataFrame:")
print(df.columns.tolist())

# Function to create comparative plots
def create_comparative_plots(input_var, constant_vars):
    # Create a figure with 4 subplots
    fig, axs = plt.subplots(2, 2, figsize=(16, 12))
    fig.suptitle(f'AODV vs R-AODV Comparison (Varying {input_var})', fontsize=16)
    
    # Output metrics to plot
    outputs = ['Throughput (kbps)', 'Delivery Ratio (%)', 'Drop Ratio (%)', 'Total Delay (s)']
    
    # Flatten the axes for easy iteration
    axs_flat = axs.flatten()
    
    # Create filters for constant variables
    constant_filter = {}
    for var, value in constant_vars.items():
        constant_filter[var] = value
    
    # Plot each output metric
    for i, output in enumerate(outputs):
        # Filter data based on constant variables
        filtered_df = df.copy()
        for var, value in constant_filter.items():
            filtered_df = filtered_df[filtered_df[var] == value]
        
        # Group and plot for each protocol type
        for protocol in ['AODV', 'R-AODV']:
            protocol_data = filtered_df[filtered_df['Type'] == protocol]
            protocol_data = protocol_data.sort_values(by=input_var)
            
            axs_flat[i].plot(protocol_data[input_var], protocol_data[output], 
                              label=protocol, marker='o')
        
        # Customize each subplot
        axs_flat[i].set_xlabel(input_var)
        axs_flat[i].set_ylabel(output)
        axs_flat[i].set_title(f'{output} vs {input_var}')
        axs_flat[i].legend()
        axs_flat[i].grid(True)
    
    plt.tight_layout()
    plt.show()

# Scenario 1: Varying Node Count (keeping Packet Rate and Velocity constant)
create_comparative_plots('Node Count', {'Packet Rate': 100, ' Velocity': 10})

# Scenario 2: Varying Velocity (keeping Node Count and Packet Rate constant)
create_comparative_plots(' Velocity', {'Node Count': 20, 'Packet Rate': 100})

# Scenario 3: Varying Packet Rate (keeping Node Count and Velocity constant)
create_comparative_plots('Packet Rate', {'Node Count': 20, ' Velocity': 10})

print("Graphs have been generated and displayed.")