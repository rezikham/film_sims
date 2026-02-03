
import os
import struct
import glob
import re

def convert_cube_to_bin(cube_path, bin_path):
    print(f"Converting {cube_path} -> {bin_path}")
    
    with open(cube_path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
        
    size = -1
    data_lines = []
    
    # Simple parser that handles comments and keywords
    for line in lines:
        line = line.strip()
        if not line or line.startswith('#') or line.startswith('TITLE') or line.startswith('DOMAIN_'):
            continue
            
        if line.startswith('LUT_3D_SIZE'):
            parts = line.split()
            if len(parts) >= 2:
                size = int(parts[1])
            continue
            
        # Try to parse data
        parts = line.split()
        if len(parts) == 3:
            try:
                r, g, b = map(float, parts)
                data_lines.append((r, g, b))
            except ValueError:
                pass

    if size == -1:
        # Infer size if not specified
        # size^3 = len(data)
        import math
        calculated_size = round(len(data_lines) ** (1/3.0))
        if calculated_size ** 3 == len(data_lines):
            size = calculated_size
            print(f"  Inferred size: {size}")
        else:
            print(f"  ERROR: Could not determine size. Data lines: {len(data_lines)}")
            return False

    expected_count = size * size * size
    if len(data_lines) != expected_count:
        print(f"  ERROR: Expected {expected_count} lines, got {len(data_lines)}")
        return False
        
    # Write .bin file
    with open(bin_path, 'wb') as f:
        # Header structure based on CubeLUTParser.kt
        # 0x00-0x07: ".MS-LUT " magic
        # 0x08-0x0B: version (1)
        # 0x0C-0x0F: LUT size (little endian int)
        # 0x10: Format hint (3 = float)
        # ... padding ...
        # 0x28-0x2F: data offset (little endian long)
        
        f.write(b".MS-LUT ")
        f.write(struct.pack('<I', 1)) # Version 1
        f.write(struct.pack('<I', size)) # Size
        f.write(struct.pack('B', 3)) # Format 3 (Float)
        
        # Padding to reach 0x28
        f.write(b'\x00' * (0x28 - f.tell()))
        
        data_offset = 64 # 0x40
        f.write(struct.pack('<Q', data_offset)) # Data offset
        
        # Padding to reach data_offset
        current_pos = f.tell()
        if current_pos < data_offset:
            f.write(b'\x00' * (data_offset - current_pos))
            
        # Write data (Little Endian, Float32, RGB)
        for r, g, b in data_lines:
            f.write(struct.pack('<fff', r, g, b))
            
    print(f"  Success! Wrote {os.path.getsize(bin_path)} bytes.")
    return True

def main():
    base_dir = "app/src/main/assets/luts/Leica_lux"
    
    # Recursive search for .cube files
    cube_files = []
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.lower().endswith(".cube"):
                cube_files.append(os.path.join(root, file))
                
    if not cube_files:
        print("No .cube files found.")
        return

    print(f"Found {len(cube_files)} .cube files.")
    
    success_count = 0
    for cube_path in cube_files:
        bin_path = os.path.splitext(cube_path)[0] + ".bin"
        if convert_cube_to_bin(cube_path, bin_path):
            success_count += 1
            
    print(f"Done. Converted {success_count}/{len(cube_files)} files.")

if __name__ == "__main__":
    main()
