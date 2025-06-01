import os
import re
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

def plot_graphs(data_directory):
    # Supported graph types
    graph_types = ['cwnd', 'inflight', 'next-rx', 'next-tx', 'rto', 'rtt', 'ssth']
    
    # Color palette for different flows
    colors = ['blue', 'red', 'green', 'purple', 'orange', 'brown', 'pink']
    
    # Create a PDF to store all plots
    with PdfPages('TcpVariantsComparison_Unified_Graphs.pdf') as pdf:
        # Iterate through each graph type
        for graph_type in graph_types:
            # Find all files matching the graph type
            matching_files = [f for f in os.listdir(data_directory) 
                              if f.startswith('TcpVariantsComparison-') 
                              and f.endswith(f'-{graph_type}.data')]
            
            # Sort files to ensure consistent order (flow0, flow1, etc.)
            matching_files.sort(key=lambda x: int(re.findall(r'flow(\d+)', x)[0]))
            
            # Create a figure for this graph type
            plt.figure(figsize=(12, 6))
            plt.title(f'{graph_type.upper()} Metric Comparison', fontsize=16)
            plt.xlabel('Time')
            plt.ylabel(graph_type.upper())
            plt.grid(True)
            
            # Plot each file
            for idx, filename in enumerate(matching_files):
                filepath = os.path.join(data_directory, filename)
                
                # Read data
                try:
                    data = np.loadtxt(filepath)
                    
                    # Plot data with a unique color
                    plt.plot(data[:, 0], data[:, 1], 
                             label=f'Flow {idx}', 
                             color=colors[idx % len(colors)])
                
                except Exception as e:
                    print(f"Error processing {filename}: {e}")
            
            # Add legend
            plt.legend()
            
            # Save to PDF
            pdf.savefig(plt.gcf())
            plt.close()
    
    print("Unified graphs have been saved to TcpVariantsComparison_Unified_Graphs.pdf")

def main():
    # Specify the directory containing your .data files
    data_directory = '.'  # Current directory, change if needed
    plot_graphs(data_directory)

if __name__ == "__main__":
    main()