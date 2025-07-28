# Secret Recovery (Shamir)

Minimal Java implementation to recover the constant term \(c\) of a polynomial from mixed-base shares.

## Files

- `Main.java` — Contains parsing, decoding, Vandermonde setup, Gaussian elimination, and error detection.
- `input.json` — One or more test-case objects.

## Usage

1. Compile:
   ```bash
   javac Main.java
   ```
2. Run:
   ```bash
   java Main
   ```

## input.json format

```json
[
  {
    "keys": {"n":4,"k":3},
    "1": {"base":"10","value":"4"},
    /* ... */
  },
  {
    "keys": {"n":10,"k":7},
    "1": {"base":"6","value":"..."},
    /* ... */
  }
]
```

Output on console:
```
Test-case #1 ⇒ c = <secret>
Test-case #2 ⇒ c = <secret>
Invalid shares: [<x1>,<x2>,...]
```

