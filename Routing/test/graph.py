import os
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

# Create output directories
os.makedirs('output', exist_ok=True)

# Read the CSV file
df = pd.read_csv('report.csv')

# Function to create plots and save to PDF
def generate_comparative_plots_pdf():
    # Open a PDF file to save all plots
    with PdfPages('output/aodv_comparison_plots.pdf') as pdf:
        # Scenarios and their constant variables
        scenarios = [
            ('Node Count', {'Packet Rate': 100, ' Velocity': 10}),
            (' Velocity', {'Node Count': 20, 'Packet Rate': 100}),
            ('Packet Rate', {'Node Count': 20, ' Velocity': 10})
        ]
        
        # Output metrics to plot
        outputs = ['Throughput (kbps)', 'Delivery Ratio (%)', 'Drop Ratio (%)', 'Total Delay (s)']
        
        # Iterate through scenarios and outputs
        for input_var, constant_vars in scenarios:
            for output in outputs:
                # Create a new figure
                plt.figure(figsize=(10, 6))
                plt.suptitle(f'AODV vs R-AODV: {output} vs {input_var}', fontsize=14)
                
                # Filter data based on constant variables
                filtered_df = df.copy()
                for var, value in constant_vars.items():
                    filtered_df = filtered_df[filtered_df[var] == value]
                
                # Plot for each protocol type
                for protocol in ['AODV', 'R-AODV']:
                    protocol_data = filtered_df[filtered_df['Type'] == protocol]
                    protocol_data = protocol_data.sort_values(by=input_var)
                    
                    plt.plot(protocol_data[input_var], protocol_data[output], 
                             label=protocol, marker='o')
                
                # Customize the plot
                plt.xlabel(input_var)
                plt.ylabel(output)
                plt.legend()
                plt.grid(True)
                
                # Add plot to PDF
                pdf.savefig()
                plt.close()
    
    print("PDF with all graphs saved to output/aodv_comparison_plots.pdf")

# Generate the PDF
generate_comparative_plots_pdf()