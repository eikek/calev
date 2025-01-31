{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.11";
    devshell-tools.url = "github:eikek/devshell-tools";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
    devshell-tools,
  }:
    {
    }
    // flake-utils.lib.eachDefaultSystem (system: let
      pkgs = nixpkgs.legacyPackages.${system};
      ciPkgs = with pkgs; [
        devshell-tools.packages.${system}.sbt17
        jdk17
      ];
      devshellPkgs =
        ciPkgs
        ++ (with pkgs; [
          jq
          scala-cli
        ]);
    in {
      formatter = pkgs.alejandra;

      devShells = {
        default = pkgs.mkShellNoCC {
          buildInputs = (builtins.attrValues devshell-tools.legacyPackages.${system}.cnt-scripts) ++ devshellPkgs;
        };
        ci = pkgs.mkShellNoCC {
          buildInputs = ciPkgs;
        };
      };
    });
}
