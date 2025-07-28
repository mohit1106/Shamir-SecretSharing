This repository contains a **Java** implementation of a simplified Shamir's Secret Sharing reconstruction algorithm, focusing on recovering the **constant term** \(c\) of an unknown polynomial given mixed-base roots.

## 📁 Files

- `Main.java` — Main Java class implementing:

  1. Manual JSON parsing of `input.json` (no external libraries).
  2. Decoding of mixed-base share values into `BigInteger`.
  3. Construction of a Vandermonde system and Gaussian elimination to recover coefficients.
  4. Extraction and printing of the constant term \(c=a_0\) for each test-case.

- `input.json` — Contains one or more test-case objects (single JSON or an array). Each object must follow this format:

  ```json
  {
    "keys": {"n": <number of shares>, "k": <threshold>},
    "1": {"base": "<base>", "value": "<encoded y>"},
    "2": {...},
    ...
  }
  ```

## ⚙️ Requirements

- Java 8 or higher (no external dependencies).
- `input.json` placed alongside `ShamirSecret.java`.

## 🚀 Usage

1. **Compile**:
   ```bash
   javac ShamirSecret.java
   ```
2. **Run**:
   ```bash
   java ShamirSecret
   ```
3. **Output**: The program prints each test-case’s recovered constant term:
   ```text
   Test-case #1 ⇒ c = <value>
   Test-case #2 ⇒ c = <value>
   ```

## 📝 Adding Test Cases

- To include multiple test scenarios, wrap your JSON objects in an array in `input.json`:

  ```json
  [
    { /* first test-case */ },
    { /* second test-case */ }
  ]
  ```

- Example with two test-cases:

  ```json
  [
    {
      "keys": {"n":4,"k":3},
      "1":{"base":"10","value":"4"},
      "2":{"base":"2","value":"111"},
      "3":{"base":"10","value":"12"},
      "6":{"base":"4","value":"213"}
    },
    {
      "keys": {"n":10,"k":7},
      "1":{"base":"6","value":"13444211440455345511"},
      /* ...more shares... */
    }
  ]
  ```

## 💡 How It Works

1. **Manual JSON Parsing** — Splits top-level objects and reads lines to extract parameters.
2. **Mixed-Base Decoding** — Uses `new BigInteger(value, base)`.
3. **Vandermonde System** — Forms \(A_{ij}=x_i^j\) and solves \(A\mathbf{a} = \mathbf{y}\) by Gaussian elimination.
4. **Constant Extraction** — The recovered `a[0]` is the secret constant \(c\).

---

© 2025 mohit_verma

